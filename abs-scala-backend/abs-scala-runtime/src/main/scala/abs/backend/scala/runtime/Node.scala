package abs.backend.scala.runtime

import akka.actor.{Actor, ActorRef}
import akka.event.EventHandler
import akka.serialization.RemoteActorSerialization
import java.net.ServerSocket

object NodeManager {
  sealed abstract class Message
  case object NewCog extends Message
  case object NewLocalCog extends Message
}

class NodeManager(var host: String, var port: Int) extends Actor {
  import NodeManager._
  
  if (port == 0) {
    val server = new ServerSocket(0);
    port = server.getLocalPort();
    server.close();
  }
    
  private val server = Actor.remote.start(host, port)
  
  host = server.address.getAddress().getHostAddress()
  
  private[runtime] def registerByUuid(actor: ActorRef) {
    server.registerByUuid(actor)
  }
  
  private[runtime] def newCog = {
    EventHandler.debug(this, "Allocating new COG")
      
    val cog = Actor.actorOf(new Cog(this)).start()
    server.registerByUuid(cog)
    val remoteRef = RemoteActorSerialization.toRemoteActorRefProtocol(Actor.remote.actorFor(cog.uuid.toString, server.address.getAddress().getHostAddress(), server.address.getPort())).toByteArray
      
    cog ! new Cog.RemoteSelfRef(remoteRef)
    
    (cog, remoteRef)
  }
  
  def receive = {
    case NewLocalCog =>
      self reply_? newCog
    case NewCog =>
      self reply_? newCog._2
  }
}

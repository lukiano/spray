package cc

import akka.actor.{ActorRef, Actor}
import java.io.File
import spray.http._
import collection.immutable.LinearSeq
import spray.utils.{PimpedClass, PimpedLinearSeq}

package object spray {

  type Route = RequestContext => Unit
  type RoutingResult = Either[Set[Rejection], HttpResponse]
  type ContentTypeResolver = (File, Option[Charset]) => ContentType
  type Marshaller = PartialFunction[Any, (List[ContentType], ContentType => RawContent)]
  type Unmarshaller[A] = ContentType => Either[List[ContentTypeRange], BufferContent => A]
  
  def actor(id: Symbol): ActorRef = actor(id.toString)

  def actor(id: String): ActorRef = {
    val actors = Actor.registry.actorsFor(id)
    assert(actors.length == 1, actors.length + " actors for id '" + id + "' found, expected exactly one")
    actors(0)
  }

  def actor[A <: Actor : Manifest]: ActorRef = {
    val actors = Actor.registry.actorsFor(manifest)
    assert(actors.length == 1, "Actor of type '" + manifest.erasure.getName + "' not found")
    actors(0)
  }
  
  private val unmanglingOperators = Map("$eq" -> "=", "$greater" -> ">", "$less" -> "<", "$plus" -> "+",
    "$minus" -> "-", "$times" -> "*", "$div" -> "/", "$bang" -> "!", "$at" -> "@", "$hash" -> "#", "$percent" -> "%",
    "$up" -> "^", "$amp" -> "&", "$tilde" -> "~", "$qmark" -> "?", "$bar" -> "|", "$bslash" -> "\\")
  
  def unmangle(name: String): String = (name /: unmanglingOperators) {
    case (n, (key, value)) => n.replace(key, value)
  }
  
  def make[A, U](a: A)(f: A => U): A = { f(a); a }
  
  implicit def pimpWithExtension(file: File): { def extension: String } = new {
    def extension = {
      val name = file.getName
      name.lastIndexOf('.') match {
        case -1 => ""
        case x => name.substring(x + 1)
      }
    }
  }
  
  implicit def pimpLinearSeq[A](seq: LinearSeq[A]): PimpedLinearSeq[A] = new PimpedLinearSeq[A](seq)
  
  implicit def pimpClass[A](clazz: Class[A]): PimpedClass[A] = new PimpedClass[A](clazz)
}
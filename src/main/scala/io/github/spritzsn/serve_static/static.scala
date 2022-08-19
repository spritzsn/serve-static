package io.github.spritzsn.example

import io.github.spritzsn.spritz.{Request, Response, Server, responseTime}
import io.github.spritzsn.body_parser.JSON
import io.github.spritzsn.cors
import io.github.spritzsn.async.loop
import cps.*
import cps.monads.FutureAsyncMonad

import java.nio.file.{Files, Path, Paths}
import scala.concurrent.Future
import scala.io.Codec

def serve(path: Path, res: Response) = async {
  if !Files.exists(path) then res.sendStatus(404)
  else if !Files.isReadable(path) then res.sendStatus(403)
  else
    val file = await(readFile(path.toString, Codec.UTF8))
    res.headers("Content-Type") = mime
    res.send(file)
}

def static(root: String) =
  import io.github.spritzsn.spritz.{HandlerResult, RequestHandler2, HandlerReturnType}
  import io.github.spritzsn.fs.readFile

  val rootpath = Paths.get(root)
  println(rootpath)

  require(Files.isDirectory(rootpath), s"static: root path '$root' is not a directory")
  require(Files.isExecutable(rootpath), s"static: root path '$root' is not a searchable directory")

  new RequestHandler2:
    def apply(req: Request, res: Response) = async {
      if req.rest.isEmpty || req.rest == "/" then
        val index = rootpath resolve "index.html"

        if !Files.exists(rootpath) then res.send("no index")
        else

      else
        val rest = if req.rest.startsWith("/") then req.rest drop 1 else req.rest
        val path = rootpath resolve rest

    }


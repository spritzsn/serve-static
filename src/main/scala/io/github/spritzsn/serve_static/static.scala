package io.github.spritzsn.serve_static

import io.github.spritzsn.spritz.{Request, Response, Server, responseTime, HandlerReturnType, RequestHandler2}
import io.github.spritzsn.fs.readFile
import io.github.spritzsn.async.loop
import cps.*
import cps.monads.FutureAsyncMonad

import java.nio.file.{Files, Path, Paths}
import scala.concurrent.Future
import scala.io.Codec

def serve(path: Path, res: Response): Future[Response] = async {
  if !Files.exists(path) then res.sendStatus(404)
  else if !Files.isReadable(path) then res.sendStatus(403)
  else
    val file = await(readFile(path.toString, Codec.UTF8))

    res.headers("Content-Type") = mime(path)
    res.send(file)
}

def static(root: String) =
  val rootpath = Paths.get(root)

  require(Files.isDirectory(rootpath), s"static: root path '$root' is not a directory")
  require(Files.isExecutable(rootpath), s"static: root path '$root' is not a searchable directory")

  new RequestHandler2:
    def apply(req: Request, res: Response): HandlerReturnType = async {
      if req.rest.isEmpty || req.rest == "/" then
        val index = rootpath resolve "index.html"

        await(serve(index, res))
      else
        val rest = if req.rest.startsWith("/") then req.rest drop 1 else req.rest
        val path = rootpath resolve rest

        await(serve(path, res))
    }

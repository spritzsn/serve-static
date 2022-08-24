package io.github.spritzsn.serve_static

import io.github.spritzsn.spritz.{
  HandlerReturnType,
  Request,
  RequestHandler2,
  Response,
  Server,
  contentType,
  responseTime,
}
import io.github.spritzsn.fs.readFile
import io.github.spritzsn.async.loop
import cps.*
import cps.monads.FutureAsyncMonad

import java.nio.file.{Files, Path, Paths}
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import scala.concurrent.Future
import scala.io.Codec

private def serve(path: Path, res: Response): Future[Response] = async {
  if !Files.exists(path) then res.sendStatus(404)
  else if !Files.isReadable(path) then res.sendStatus(403)
  else
    res.set("Content-Type", mime(path)).send(await(readFile(path.toString, Codec.UTF8)))
    res.set(
      "Last-Modified",
      DateTimeFormatter.RFC_1123_DATE_TIME.format(Files.getLastModifiedTime(path).toInstant.atZone(res.zoneId)),
    )
}

def apply(root: String) =
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

private def mime(path: Path): String =
  val filename = path.getFileName.toString
  val extension =
    filename lastIndexOf '.' match
      case -1  => ""
      case idx => filename.substring(idx + 1)

  contentType(extension)
end mime

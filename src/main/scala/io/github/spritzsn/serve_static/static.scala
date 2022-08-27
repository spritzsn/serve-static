package io.github.spritzsn.serve_static

import io.github.spritzsn.spritz.{RequestHandler, Response, Server, contentType}
import io.github.spritzsn.fs.readFile
import io.github.spritzsn.async.loop
import cps.*
import cps.monads.FutureAsyncMonad

import java.nio.file.{Files, Path, Paths}
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import scala.concurrent.Future
import scala.io.Codec

def apply(root: String): RequestHandler =
  val rootpath = Paths.get(root)

  require(Files.isDirectory(rootpath), s"static: root path '$root' is not a directory")
  require(Files.isExecutable(rootpath), s"static: root path '$root' is not a searchable directory")

  (req, res) =>
    if req.rest.isEmpty || req.rest == "/" then
      val index = rootpath resolve "index.html"

      serve(index, res)
    else
      val rest = if req.rest.startsWith("/") then req.rest drop 1 else req.rest
      val path = rootpath resolve rest

      serve(path, res)

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

private def mime(path: Path): String =
  val filename = path.getFileName.toString
  val extension =
    filename lastIndexOf '.' match
      case -1  => ""
      case idx => filename.substring(idx + 1)

  contentType(extension)

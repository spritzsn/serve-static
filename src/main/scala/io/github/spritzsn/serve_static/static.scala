package io.github.spritzsn.serve_static

import io.github.spritzsn.spritz.{HandlerResult, HandlerReturnType, RequestHandler, Response, Server, contentType}
import io.github.spritzsn.fs.readFile
import io.github.spritzsn.async.loop
import cps.*
import cps.monads.FutureAsyncMonad

import java.nio.file.{Files, Path, Paths}
import java.time.{Instant, ZoneOffset}
import java.time.format.DateTimeFormatter
import scala.concurrent.Future
import scala.io.Codec

def apply(
    root: String,
    lastModified: Boolean = true,
    dotfiles: "allow" | "deny" | "ignore" = "ignore",
): RequestHandler =
  val rootpath = (Paths get root normalize).toAbsolutePath

  require(Files isDirectory rootpath, s"static: root path '$root' is not a directory")
  require(Files isExecutable rootpath, s"static: root path '$root' is not a searchable directory")
  require(Files isReadable rootpath, s"static: root path '$root' is not a readable directory")

  (req, res) =>
    def serve(path: Path): HandlerReturnType = async {
      if !path.normalize.toAbsolutePath.startsWith(rootpath) then
        res sendStatus 403
        HandlerResult.Next
      else if !Files.exists(path) then
        res sendStatus 404
        HandlerResult.Next
      else if !Files.isReadable(path) then
        res sendStatus 403
        HandlerResult.Next
      else if dotfiles == "deny" && path.getFileName.startsWith(".") then
        res sendStatus 403
        HandlerResult.Next
      else if dotfiles == "ignore" && path.getFileName.startsWith(".") then
        res sendStatus 404
        HandlerResult.Next
      else
        req get "If-Modified-Since" match
          case Some(date)
              if !(Files.getLastModifiedTime(path).toInstant isAfter
                Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(date))) =>
            res sendStatus 304
          case _ =>
            val filename = path.getFileName.toString
            val extension =
              filename lastIndexOf '.' match
                case -1  => ""
                case idx => filename.substring(idx + 1)
            val typ = contentType(extension)
            val content =
              if typ startsWith "text" then res.send(await(readFile(path.toString, Codec.UTF8)))
              else res.send(await(readFile(path.toString)))

            res.set("Content-Type", typ)

            if lastModified then
              res.set(
                "Last-Modified",
                DateTimeFormatter.RFC_1123_DATE_TIME.format(
                  Files.getLastModifiedTime(path).toInstant.atZone(res.zoneId),
                ),
              )
    }

    if req.rest.isEmpty || req.rest == "/" then serve(rootpath resolve "index.html")
    else
      val rest = if req.rest startsWith "/" then req.rest drop 1 else req.rest

      serve(rootpath resolve rest)

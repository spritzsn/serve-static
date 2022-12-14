package io.github.spritzsn.serve_static

import io.github.spritzsn.spritz.Server

@main def run(): Unit =
  Server("ExampleServer/1.0") { app =>
    app.use("/", apply("website"))
    app.listen(3000)
    println("listening")
  }

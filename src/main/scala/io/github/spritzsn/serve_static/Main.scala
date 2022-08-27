//package io.github.spritzsn.serve_static
//
//import io.github.spritzsn.spritz.Server
//
//@main def run(): Unit =
//  Server("ExampleServer/1.0") { app =>
//    app
//      .use("/project", apply("project"))
//      .get("/", (_, res) => res.send("hello"))
//      .post("/", (req, res) => res.send(req.body))
//    app.listen(3000)
//    println("listening")
//  }

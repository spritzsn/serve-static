//package io.github.spritzsn.serve_static
//
//import io.github.spritzsn.spritz.{Request, Response, Server}
//
//@main def run(): Unit =
//  Server("ExampleServer/1.0") { app =>
//    app
//      .use("/project", apply("project"))
//      .get("/", (_: Request, res: Response) => res.send("hello"))
//      .post("/", (req: Request, res: Response) => res.send(req.body))
//    app.listen(3000)
//    println("listening")
//  }

import gleam/float
import gleam/int
import gleam/io
import gleam/option
import gleam/result
import savoiardi
import tiramisu
import tiramisu/background
import tiramisu/camera
import tiramisu/effect.{type Effect}
import tiramisu/geometry
import tiramisu/input
import tiramisu/light
import tiramisu/scene
import tiramisu/texture
import tiramisu/transform
import trickster_studio/editor/spell_circle_widget.{spell_circle_widget}
import trickster_studio/fragment
import trickster_studio/spell_tree_map
import vec/vec2
import vec/vec3

const test_spell = "YxEpKcpMzi4uSS2yKi5IzcmJL0gsKhFECBYklgCpPAYgYGLkBJLMICYjI3YlVJBnQiIYeahlIPUUMTEAAACpPxc0AQAA"

pub type Model {
  Model(
    spell: spell_tree_map.SpellTreeDepthMap,
    font: option.Option(savoiardi.Font),
    circle_texture: option.Option(savoiardi.Texture),
    circle_camera: spell_circle_widget.Camera,
  )
}

pub type Msg {
  Tick
  BackgroundSet
  FontLoaded(savoiardi.Font)
  FontDidNotLoad
  CircleTextureLoaded(savoiardi.Texture)
  CircleTextureDidNotLoad
}

pub fn main() -> Nil {
  let assert Ok(Nil) =
    tiramisu.application(init:, update:, view:)
    |> tiramisu.start("#app", tiramisu.FullScreen, option.None)
  Nil
}

fn init(ctx: tiramisu.Context) -> #(Model, Effect(Msg), option.Option(_)) {
  let assert Ok(fragment.SpellPartFragment(spell)) =
    fragment.from_base64(test_spell)

  let bg_effect =
    background.set(
      ctx.scene,
      background.Color(0x1a1a2e),
      BackgroundSet,
      BackgroundSet,
    )

  let load_font =
    geometry.load_font(
      "/helvetiker_regular.typeface.json",
      FontLoaded,
      FontDidNotLoad,
    )

  let load_texture =
    texture.load("/circle_48.png", CircleTextureLoaded, CircleTextureDidNotLoad)

  #(
    Model(
      spell: spell_tree_map.to_spell_tree_depth_map(spell),
      font: option.None,
      circle_texture: option.None,
      circle_camera: spell_circle_widget.Camera(1.0, 0.0, 0.0),
    ),
    // effect.batch([bg_effect, effect.dispatch(Tick)]),
    effect.batch([bg_effect, load_texture, load_font, effect.dispatch(Tick)]),
    option.None,
  )
}

fn update(
  model: Model,
  msg: Msg,
  ctx: tiramisu.Context,
) -> #(Model, Effect(Msg), option.Option(_)) {
  case msg {
    Tick -> {
      let circle_camera =
        update_circle_camera(ctx.input, ctx.canvas_size, model.circle_camera)

      let model = Model(..model, circle_camera:)

      #(model, effect.dispatch(Tick), option.None)
    }
    BackgroundSet -> #(model, effect.none(), option.None)
    FontLoaded(font) -> #(
      Model(..model, font: option.Some(font)),
      effect.none(),
      option.None,
    )
    FontDidNotLoad -> {
      io.println("font failed to load")
      #(model, effect.dispatch(Tick), option.None)
    }
    CircleTextureLoaded(circle_texture) -> #(
      Model(
        ..model,
        circle_texture: option.Some(texture.set_filter_mode(
          circle_texture,
          texture.NearestFilter,
          texture.NearestFilter,
        )),
      ),
      effect.none(),
      option.None,
    )
    CircleTextureDidNotLoad -> {
      io.println("circle texture failed to load")
      #(model, effect.dispatch(Tick), option.None)
    }
  }
}

fn view(model: Model, ctx: tiramisu.Context) -> scene.Node {
  let cam =
    camera.camera_2d(size: vec2.Vec2(
      float.round(ctx.canvas_size.x),
      float.round(ctx.canvas_size.y),
    ))

  let circle_transform =
    transform.at(vec3.Vec3(
      model.circle_camera.pan_x,
      model.circle_camera.pan_y,
      0.0,
    ))
    |> transform.scale_uniform(model.circle_camera.zoom)

  let log2 = fn(x: Float) {
    use lhs <- result.try(float.logarithm(x))
    use rhs <- result.map(float.logarithm(2.0))
    lhs /. rhs
  }

  let depth =
    model.circle_camera.zoom
    |> log2()
    |> result.unwrap(0.0)
    |> float.floor()
    |> float.round()
    |> int.divide(3)
    |> result.unwrap(0)

  let alpha_getter = fn(size: Float) {
    let true_size = size *. model.circle_camera.zoom *. 200.0

    let alpha =
      float.min(
        ctx.canvas_size.y /. { true_size *. 2.0 } -. 0.1,
        result.unwrap(float.power(true_size, 1.2), 0.0)
          /. ctx.canvas_size.y
          +. 0.1,
      )
    float.min(float.max(alpha, 0.0), 1.0)
  }

  let text_size_getter = fn(size: Float) {
    let true_size = size *. model.circle_camera.zoom *. 200.0

    true_size
  }

  scene.empty(id: "Scene", transform: transform.identity, children: [
    scene.camera(
      id: "camera",
      camera: cam,
      transform: transform.at(vec3.Vec3(0.0, 0.0, 20.0)),
      active: True,
      viewport: option.None,
      postprocessing: option.None,
    ),
    scene.light(
      id: "ambient",
      light: {
        let assert Ok(light) = light.ambient(color: 0xffffff, intensity: 1.0)
        light
      },
      transform: transform.identity,
    ),
    spell_circle_widget(
      model.spell,
      model.font,
      model.circle_texture,
      circle_transform,
      depth,
      alpha_getter,
      text_size_getter,
    ),
  ])
}

fn update_circle_camera(
  input: input.InputState,
  viewport: vec2.Vec2(Float),
  circle_camera: spell_circle_widget.Camera,
) -> spell_circle_widget.Camera {
  let spell_circle_widget.Camera(zoom:, pan_x:, pan_y:) = circle_camera

  let scroll = input.mouse_wheel_delta(input) /. -100.0
  let vec2.Vec2(mouse_x, mouse_y) = input.mouse_position(input)
  let local_mouse_x = mouse_x -. viewport.x /. 2.0
  let local_mouse_y = -1.0 *. { mouse_y -. viewport.y /. 2.0 }

  case scroll {
    0.0 -> circle_camera

    _ -> {
      let new_pan_x = pan_x +. { scroll *. { pan_x -. local_mouse_x } } /. 10.0
      let new_pan_y = pan_y +. { scroll *. { pan_y -. local_mouse_y } } /. 10.0

      let new_zoom = zoom +. { scroll *. zoom } /. 10.0
      spell_circle_widget.Camera(
        zoom: new_zoom,
        pan_x: new_pan_x,
        pan_y: new_pan_y,
      )
    }
  }
}

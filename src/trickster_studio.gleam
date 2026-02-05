import gleam/dict
import gleam/float
import gleam/int
import gleam/io
import gleam/option
import gleam/result
import gleam_community/maths
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
import trickster_studio/pattern
import trickster_studio/spell_tree_map
import vec/vec2
import vec/vec3

@external(javascript, "./trickster_studio/js_utils_ffi.mjs", "get_url_search_params")
pub fn get_url_search_params() -> List(#(String, String))

@external(javascript, "./trickster_studio/js_utils_ffi.mjs", "set_transparent_bg")
pub fn set_transparent_bg(renderer: savoiardi.Renderer) -> Nil

pub type Model {
  Model(
    spell: spell_tree_map.SpellTreeDepthMap,
    font: option.Option(savoiardi.Font),
    circle_texture: option.Option(savoiardi.Texture),
    pattern_literal_texture: option.Option(savoiardi.Texture),
    circle_camera: spell_circle_widget.Camera,
    mouse_pos: vec2.Vec2(Float),
  )
}

pub type Msg {
  Tick
  BackgroundSet
  FontLoaded(savoiardi.Font)
  FontDidNotLoad
  CircleTextureLoaded(savoiardi.Texture)
  CircleTextureDidNotLoad
  PatternLiteralTextureLoaded(savoiardi.Texture)
  PatternLiteralTextureDidNotLoad
}

pub fn main() -> Nil {
  let assert Ok(Nil) =
    tiramisu.application(init:, update:, view:)
    |> tiramisu.start("#app", tiramisu.FullScreen, option.None)
  Nil
}

fn init(ctx: tiramisu.Context) -> #(Model, Effect(Msg), option.Option(_)) {
  let search_params = dict.from_list(get_url_search_params())

  let spell = case dict.get(search_params, "spell") {
    Ok(spell_string) ->
      case fragment.from_base64(spell_string) {
        Ok(fragment.SpellPartFragment(spell_part)) -> spell_part
        Ok(fragment) -> fragment.SpellPart(fragment, [])
        _ -> fragment.empty_spell_part
      }
    Error(_) -> fragment.empty_spell_part
  }

  let bg_effect = case dict.get(search_params, "transparent") {
    Ok("true") | Ok("True") -> effect.none()
    _ ->
      background.set(
        ctx.scene,
        background.Color(0x1a1a2e),
        BackgroundSet,
        BackgroundSet,
      )
  }

  set_transparent_bg(ctx.renderer)

  let load_font =
    geometry.load_font(
      "/helvetiker_regular.typeface.json",
      FontLoaded,
      FontDidNotLoad,
    )

  let load_circle_texture =
    texture.load("/circle_48.png", CircleTextureLoaded, CircleTextureDidNotLoad)

  let load_pattern_literal_texture =
    texture.load(
      "/pattern_literal.png",
      PatternLiteralTextureLoaded,
      PatternLiteralTextureDidNotLoad,
    )

  #(
    Model(
      spell: spell_tree_map.to_spell_tree_depth_map(spell),
      font: option.None,
      circle_texture: option.None,
      pattern_literal_texture: option.None,
      circle_camera: spell_circle_widget.Camera(0.3, 0.0, 0.0),
      mouse_pos: vec2.Vec2(0.0, 0.0),
    ),
    // effect.batch([bg_effect, effect.dispatch(Tick)]),
    effect.batch([
      bg_effect,
      load_circle_texture,
      load_pattern_literal_texture,
      load_font,
      effect.dispatch(Tick),
    ]),
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
      let mouse_pos = input.mouse_position(ctx.input)

      let circle_camera =
        update_circle_camera(
          ctx.input,
          ctx.canvas_size,
          model.circle_camera,
          model.mouse_pos,
        )

      let model = Model(..model, mouse_pos:, circle_camera:)

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
    PatternLiteralTextureLoaded(pattern_literal_texture) -> #(
      Model(
        ..model,
        pattern_literal_texture: option.Some(texture.set_filter_mode(
          pattern_literal_texture,
          texture.NearestFilter,
          texture.NearestFilter,
        )),
      ),
      effect.none(),
      option.None,
    )
    PatternLiteralTextureDidNotLoad -> {
      io.println("pattern literal texture failed to load")
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
    let viewed_size = size *. model.circle_camera.zoom

    float.min(
      result.unwrap(float.power(viewed_size +. 0.7, 4.0), 0.0),
      result.unwrap(float.power(maths.e(), -6.0 *. { viewed_size -. 0.3 }), 0.0),
    )
    |> float.clamp(0.0, 0.8)
  }

  let text_size_getter = fn(size: Float) {
    let true_size = size *. model.circle_camera.zoom *. ctx.canvas_size.y
    true_size *. 0.5
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
      ctx.canvas_size,
      "spell editor",
      model.circle_texture,
      model.pattern_literal_texture,
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
  prev_mouse_pos: vec2.Vec2(Float),
) -> spell_circle_widget.Camera {
  let spell_circle_widget.Camera(zoom:, pan_x:, pan_y:) = circle_camera

  let scroll = input.mouse_wheel_delta(input) /. -100.0
  let vec2.Vec2(mouse_x, mouse_y) = input.mouse_position(input)
  let local_mouse_x = mouse_x -. viewport.x /. 2.0
  let local_mouse_y = -1.0 *. { mouse_y -. viewport.y /. 2.0 }

  let scroll_transform = case scroll {
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

  let mouse_x_delta = mouse_x -. prev_mouse_pos.x
  let mouse_y_delta = prev_mouse_pos.y -. mouse_y

  let mouse_down = input.is_left_button_pressed(input)

  let pan_transform = case mouse_down {
    True -> {
      spell_circle_widget.Camera(
        zoom: 0.0,
        pan_x: mouse_x_delta,
        pan_y: mouse_y_delta,
      )
    }
    False -> spell_circle_widget.Camera(zoom: 0.0, pan_x: 0.0, pan_y: 0.0)
  }

  spell_circle_widget.Camera(
    zoom: scroll_transform.zoom +. pan_transform.zoom,
    pan_x: scroll_transform.pan_x +. pan_transform.pan_x,
    pan_y: scroll_transform.pan_y +. pan_transform.pan_y,
  )
}

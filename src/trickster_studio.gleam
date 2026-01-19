/// 2D Game Example - Orthographic Camera
import gleam/float
import gleam/io
import gleam/option
import gleam/time/duration
import savoiardi
import tiramisu
import tiramisu/background
import tiramisu/camera
import tiramisu/effect.{type Effect}
import tiramisu/geometry
import tiramisu/light
import tiramisu/material
import tiramisu/scene
import tiramisu/texture
import tiramisu/transform
import trickster_studio/editor/spell_circle_widget.{spell_circle_widget}
import trickster_studio/fragment
import trickster_studio/spell_tree_map
import vec/vec2
import vec/vec3

const test_spell = "5VfBTsJAEJ1diilGEYkHvXjwA7yYePBiDz1yIMY7qYYDsZCm1PtqYoKJF/+A+AXKF/ST/ARKQLa13dnZpgQTm0ADM53pvHlvZ9c6isLB/cM46odX46Dv+73AC6ND+WfgRclt9NXhdc4uAKCWfICxvMunDXXMfmPHlrTT8ybOx1jcD3CeMDsAB/S9O4DawRU7aF2TeI+QX/m1erApH/QH44i15O/R4/CuH15/w8+FF8Tc5xdlmpOce88fJDfP73ZyzvvSeegFhfGK8HJ26XYiD7p6HsxE/ErnJzHvgrR1VsMhSdvV+CZmkXNH6EKNKlbuKEmZy1FxquhS1L4Ytdu3kI2UDkdE3eFwxlkDLQgctNtrtOlV6/oi1HX9Ei8BRWwpMNLE6VY00fh3mqClbgFQUoM71dDDMU4t1qk1WMrQSqe2qci3vlxYf3W5wN0kX1C3dW+LUKp28SnErnwLK53/bSzuLLMPI8ZtT4TFca06NuBaVrIit4UDeeEYzbhI0VWt+QXzq253FaNpuW2/xNLotu3vb0KjWEIt2SDGQtlsG8wgbU1Ek1PHJLoWJpHOlaBwJWnLH0iy04zGQrPTZAVzh75Xkc6E8xGxuws11EpsghCw2QEqP6HrGWEgTgVoNCobX/p8bDDqiGAvnTUoguZVNj8n89nT72xQ6/LhOTgRLH3rEQAA"

pub type Model {
  Model(
    spell: spell_tree_map.SpellTreeDepthMap,
    font: option.Option(savoiardi.Font),
    circle_texture: option.Option(savoiardi.Texture),
    viewport: spell_circle_widget.ViewPort,
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
      viewport: spell_circle_widget.ViewPort(0.0, 0.0, 0.0)
    ),
    // effect.batch([bg_effect, effect.dispatch(Tick)]),
    effect.batch([bg_effect, load_texture, load_font]),
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

  scene.empty(id: "Scene", transform: transform.identity, children: [
    scene.camera(
      id: "camera",
      camera: cam,
      transform: transform.at(position: vec3.Vec3(model.viewport.pan_x, model.viewport.pan_y, 20.0)),
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
    spell_circle_widget(model.spell, model.font, model.circle_texture),
  ])
}

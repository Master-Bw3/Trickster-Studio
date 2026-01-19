import gleam/dict
import gleam/float
import gleam/int
import gleam/list
import gleam/option
import gleam/pair
import gleam/result
import gleam_community/maths
import savoiardi
import tiramisu/geometry
import tiramisu/material
import tiramisu/scene
import tiramisu/transform
import trickster_studio/fragment
import trickster_studio/spell_tree_map
import vec/vec2
import vec/vec3

pub type Camera {
  Camera(zoom: Float, pan_x: Float, pan_y: Float)
}

pub fn spell_circle_widget(
  spell: spell_tree_map.SpellTreeDepthMap,
  font: option.Option(savoiardi.Font),
  circle_texture: option.Option(savoiardi.Texture),
  transform: transform.Transform,
) -> scene.Node {
  spell
  |> spell_tree_map.entries
  |> list.flat_map(fn(x) { pair.second(x) |> dict.to_list })
  |> place_circles(font, circle_texture)
  |> scene.empty("spell circle", transform, _)
}

fn place_circles(
  nodes: List(#(List(Int), spell_tree_map.Node)),
  font: option.Option(savoiardi.Font),
  circle_texture: option.Option(savoiardi.Texture),
) -> List(scene.Node) {
  let assert Ok(sprite_geom) = geometry.plane(size: vec2.Vec2(500.0, 500.0))
  let assert Ok(sprite_mat) =
    material.basic(
      color: 0xffffff,
      transparent: True,
      opacity: 1.0,
      map: circle_texture,
      side: material.FrontSide,
      alpha_test: 0.0,
      depth_write: True,
    )

  let assert Ok(text_mat) =
    material.basic(
      color: 0xffffff,
      transparent: False,
      opacity: 1.0,
      map: option.None,
      side: material.FrontSide,
      alpha_test: 0.0,
      depth_write: True,
    )

  let text_geom = fn(string) {
    let assert Ok(txt) =
      case font {
        option.Some(font) ->
          geometry.text(
            text: string,
            font:,
            size: 50.0,
            depth: 0.2,
            curve_segments: 12,
            bevel_enabled: True,
            bevel_thickness: 0.05,
            bevel_size: 0.02,
            bevel_offset: 0.0,
            bevel_segments: 5,
          )
        option.None -> Error(geometry.EmptyText)
      }
      |> result.or(geometry.circle(0.1, 3))

    txt
  }

  let children =
    list.map(nodes, fn(node) {
      let #(address, spell_tree_map.Node(fragment: _, sibling_count_stack:)) =
        node
      let transform = calculate_transform(address, sibling_count_stack)
      let address_string =
        list.fold(address, "", fn(acc, i) { acc <> int.to_string(i) <> ", " })

      scene.empty("circle[" <> address_string <> "] c", transform, [
        scene.mesh(
          id: "circle[" <> address_string <> "]",
          geometry: sprite_geom,
          material: sprite_mat,
          transform: transform.identity,
          physics: option.None,
        ),
        scene.mesh(
          id: "circle[" <> address_string <> "] f",
          geometry: text_geom(address_string),
          material: text_mat,
          transform: transform.at(vec3.Vec3(0.0, 0.0, 0.1)),
          physics: option.None,
        ),
      ])
    })

  children
}

fn calculate_transform(
  address: List(Int),
  sibling_count_stack: List(Int),
) -> transform.Transform {
  calculate_transform_rec(
    list.reverse(address),
    list.reverse(sibling_count_stack),
    maths.pi() /. 2.0,
    transform.identity,
  )
}

fn calculate_transform_rec(
  address: List(Int),
  sibling_count_stack: List(Int),
  angle: Float,
  transform_acc: transform.Transform,
) -> transform.Transform {
  case address, sibling_count_stack {
    [index, ..rest_address], [sibling_count, ..rest_siblings] -> {
      let #(transform, new_angle) = case index {
        0 -> #(
          transform.with_scale(transform.identity, vec3.Vec3(0.5, 0.5, 0.5)),
          angle,
        )

        _ -> {
          let new_angle =
            angle
            -. { 2.0 *. maths.pi() }
            /. int.to_float(sibling_count)
            *. int.to_float(index - 1)

          let x = maths.cos(new_angle) *. 0.5
          let y = maths.sin(new_angle) *. 0.5
          let size =
            float.min(0.5, 1.0 /. { int.to_float(sibling_count + 1) /. 2.0 })

          let current_size = transform.scale(transform_acc).x *. size

          let transform =
            transform.at(vec3.Vec3(
              1000.0 *. current_size *. x,
              1000.0 *. current_size *. y,
              0.0,
            ))
            |> transform.scale_uniform(size)

          #(transform, new_angle +. maths.pi() /. 2.0)
        }
      }

      calculate_transform_rec(
        rest_address,
        rest_siblings,
        new_angle,
        transform.compose(transform_acc, transform),
      )
    }

    // actually [], []
    _, _ -> transform_acc
  }
}

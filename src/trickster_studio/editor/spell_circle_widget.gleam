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
import trickster_studio/editor/fragment_renderer
import trickster_studio/spell_tree_map
import vec/vec2
import vec/vec3

pub const node_render_depth = 8

pub type Camera {
  Camera(zoom: Float, pan_x: Float, pan_y: Float)
}

pub fn spell_circle_widget(
  spell: spell_tree_map.SpellTreeDepthMap,
  font: option.Option(savoiardi.Font),
  circle_texture: option.Option(savoiardi.Texture),
  transform: transform.Transform,
  depth: Int,
  alpha_getter: fn(Float) -> Float,
  text_size_getter: fn(Float) -> Float,
) -> scene.Node {
  spell
  |> spell_tree_map.entries
  |> list.filter(fn(entry) {
    entry.0 >= depth && entry.0 <= depth + node_render_depth
  })
  |> list.flat_map(fn(x) { pair.second(x) |> dict.to_list })
  |> place_circles(font, circle_texture, alpha_getter, text_size_getter)
  |> scene.empty("spell circle", transform, _)
}

fn place_circles(
  nodes: List(#(List(Int), spell_tree_map.Node)),
  font: option.Option(savoiardi.Font),
  circle_texture: option.Option(savoiardi.Texture),
  alpha_getter: fn(Float) -> Float,
  text_size_getter: fn(Float) -> Float,
) -> List(scene.Node) {
  let assert Ok(sprite_geom) = geometry.plane(size: vec2.Vec2(500.0, 500.0))
  let sprite_mat = fn(opacity) {
    let assert Ok(mat) =
      material.basic(
        color: 0xffffff,
        transparent: True,
        opacity:,
        map: circle_texture,
        side: material.FrontSide,
        alpha_test: 0.0,
        depth_write: True,
      )

    mat
  }

  let assert Ok(divider_geom) = geometry.plane(size: vec2.Vec2(15.0, 80.0))
  let divider_mat = fn(opacity) {
    let assert Ok(mat) =
      material.basic(
        color: 0x7F7FFF,
        transparent: True,
        opacity:,
        map: option.None,
        side: material.FrontSide,
        alpha_test: 0.0,
        depth_write: True,
      )

    mat
  }

  let children =
    list.map(nodes, fn(node) {
      let #(
        address,
        spell_tree_map.Node(fragment:, sibling_count_stack:, child_count:),
      ) = node
      let #(transform, angle) =
        calculate_transform(address, sibling_count_stack)
      let size = transform.scale(transform).x
      let address_string =
        list.fold(address, "", fn(acc, i) { acc <> int.to_string(i) <> ", " })

      let glyph = case fragment {
        option.Some(fragment) -> [
          fragment_renderer.render_fragment(
            fragment,
            "glyph[" <> address_string <> "]",
            size,
            alpha_getter,
            text_size_getter,
          ),
        ]
        option.None -> []
      }

      let divider = case child_count {
        0 -> []

        _ -> {
          let new_angle =
            { angle +. maths.pi() /. 2.0 }
            +. { 2.0 *. maths.pi() }
            /. int.to_float(child_count)
            *. 0.5
            -. { maths.pi() /. 2.0 }

          let x = maths.cos(new_angle) *. 235.0
          let y = maths.sin(new_angle) *. 235.0

          let divider_transform =
            transform.at(vec3.Vec3(x, y, 0.0))
            |> transform.rotate_z(new_angle -. maths.pi() /. 2.0)

          [
            scene.mesh(
              id: "divider[" <> address_string <> "]",
              geometry: divider_geom,
              material: divider_mat(alpha_getter(size) /. 4.0),
              transform: divider_transform,
              physics: option.None,
            ),
          ]
        }
      }

      let circle = [
        scene.mesh(
          id: "circle[" <> address_string <> "]",
          geometry: sprite_geom,
          material: sprite_mat(alpha_getter(size)),
          transform: transform.identity,
          physics: option.None,
        ),
      ]

      scene.empty(
        "container[" <> address_string <> "]",
        transform,
        [
          circle,
          glyph,
          divider,
        ]
          |> list.flatten,
      )
    })

  children
}

pub fn calculate_transform(
  address: List(Int),
  sibling_count_stack: List(Int),
) -> #(transform.Transform, Float) {
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
) -> #(transform.Transform, Float) {
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
    _, _ -> #(transform_acc, angle)
  }
}

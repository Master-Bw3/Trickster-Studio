import gleam/int
import lustre
import lustre/attribute
import lustre/effect.{type Effect}
import lustre/element.{type Element}
import lustre/element/html
import lustre/event

pub fn main() {
  let app = lustre.application(init, update, view)
  let assert Ok(_) = lustre.start(app, "#app", Nil)

  Nil
}

type Model {
  Model(count: Int)
}

fn init(_args) -> #(Model, Effect(Msg)) {
  let model = Model(count: 0)

  #(model, effect.none())
}

type Msg {
  UserPressedIncrement
  UserPressedDecrement
}

fn update(model: Model, msg: Msg) -> #(Model, Effect(Msg)) {
  case msg {
    UserPressedIncrement -> #(
      Model(..model, count: model.count + 1),
      effect.none(),
    )
    UserPressedDecrement -> #(
      Model(..model, count: model.count - 1),
      effect.none(),
    )
  }
}

fn view(model: Model) -> Element(Msg) {
  html.div([], [
    html.p([], [html.text(int.to_string(model.count))]),
    html.button([event.on_click(UserPressedIncrement)], [html.text("+")]),
    html.button([event.on_click(UserPressedDecrement)], [html.text("-")]),
  ])
}

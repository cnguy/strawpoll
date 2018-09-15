let component = ReasonReact.statelessComponent("NotFound");

let make = _children => {
  ...component,
  render: _self =>
    <h1 className="not-found">
      {ReasonReact.string("This page does not exist!")}
    </h1>,
};
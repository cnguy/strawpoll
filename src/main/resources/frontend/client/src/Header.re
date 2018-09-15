let component = ReasonReact.statelessComponent("Header");

let make = _children => {
  ...component,
  render: _self =>
    <div className="header">
      <button className="new" onClick={_ => ReasonReact.Router.push("/")}>
        {ReasonReact.string("New")}
      </button>
    </div>,
};
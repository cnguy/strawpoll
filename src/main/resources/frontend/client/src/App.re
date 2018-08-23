let component = ReasonReact.statelessComponent("App");

module RouterConfig = {
  type route =
    | NewPoll
    | Poll(int)
    | PollResults(int)
    | NotFound;

  let routeFromUrl = (url: ReasonReact.Router.url) =>
    switch (url.path, url.search) {
    | ([], "") => NewPoll
    | (["p", id], "") => Poll(int_of_string(id))
    | (["r", id], "") => PollResults(int_of_string(id))
    | _ => NotFound
    };

  let routeToUrl = (route: route) =>
    switch (route) {
    | NewPoll => "/"
    | Poll(id) => "/p" ++ string_of_int(id)
    | PollResults(id) => "/r/" ++ string_of_int(id)
    | NotFound => "/404"
    };
};

module Router = ReRoute.CreateRouter(RouterConfig);

let make = _children => {
  ...component,
  render: _self =>
    <Router.Container>
      ...{
           (~currentRoute) =>
             switch (currentRoute) {
             | RouterConfig.NewPoll => <NewPoll />
             | RouterConfig.Poll(id) => <Poll id />
             | RouterConfig.PollResults(id) => <PollResults id />
             | RouterConfig.NotFound => ReasonReact.string("NotFound")
             }
         }
    </Router.Container>,
};
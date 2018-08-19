let component = ReasonReact.statelessComponent("App");

module RouterConfig = {
  type route =
    | Home
    | Poll(int)
    | PollResults(int)
    | NotFound;

  let routeFromUrl = (url: ReasonReact.Router.url) =>
    switch (url.path, url.search) {
    | ([], _) => Home
    | (["poll", id], "") => Poll(int_of_string(id))
    | (["results", id], "") => PollResults(int_of_string(id))
    | _ => NotFound
    };

  let routeToUrl = (route: route) =>
    switch (route) {
    | Home => "/"
    | Poll(id) => "/poll/" ++ string_of_int(id)
    | PollResults(id) => "/results/" ++ string_of_int(id)
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
             | RouterConfig.Home => ReasonReact.string("Home")
             | RouterConfig.Poll(id) => <Poll id />
             | RouterConfig.PollResults(id) => <Poll id showResults=true />
             | RouterConfig.NotFound => ReasonReact.string("NotFound")
             }
         }
    </Router.Container>,
};
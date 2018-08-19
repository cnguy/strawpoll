open Types;

type state = {
  poll: option(poll),
  optionId: int,
};

type action =
  | SetPoll(option(poll))
  | Vote(int)
  | Submit;

let component = ReasonReact.reducerComponent("Poll");

let make = (~id: int, ~showResults: bool=false, _children) => {
  ...component,
  initialState: () => {poll: None, optionId: 0},
  didMount: self =>
    PollService.getPoll(id, maybePoll => self.send(SetPoll(maybePoll))),
  reducer: (action, state) =>
    switch (action) {
    | SetPoll(poll) => ReasonReact.Update({...state, poll})
    | Vote((optionId: int)) => ReasonReact.Update({...state, optionId})
    | Submit => NoUpdate
    },
  render: self =>
    switch (self.state.poll) {
    | Some(poll) =>
      if (showResults) {
        ReasonReact.string(
          "Showing results for poll: " ++ string_of_int(poll.id),
        );
      } else {
        ReasonReact.string(
          "Showing actual poll for poll: " ++ string_of_int(poll.id),
        );
      }
    | None => ReasonReact.string("Poll not found.")
    },
};
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
    | Some((poll: poll)) =>
      if (showResults) {
        let answers =
          poll.answers
          |> List.map((answer: answer) =>
               <li>
                 {ReasonReact.string(answer.response ++ ": ")}
                 {ReasonReact.string(string_of_int(answer.count))}
               </li>
             );
        <>
          {ReasonReact.string(poll.question)}
          <ul> {ReasonReact.array(Array.of_list(answers))} </ul>
        </>;
      } else {
        let answers =
          poll.answers
          |> List.sort((a, b) => a.rank - b.rank)
          |> List.map((answer: answer) =>
               <li>
                 {ReasonReact.string(answer.response ++ ": ")}
                 {ReasonReact.string(string_of_int(answer.count))}
               </li>
             );
        <>
          {ReasonReact.string("Vote on: " ++ poll.question)}
          <ul> {ReasonReact.array(Array.of_list(answers))} </ul>
        </>;
      }
    | None => ReasonReact.string("Poll not found.")
    },
};
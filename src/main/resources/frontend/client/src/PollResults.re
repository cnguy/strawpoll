open Types;

type state = {poll: option(poll)};

type action =
  | SetPoll(option(poll));

let component = ReasonReact.reducerComponent("PollResults");

let make = (~id: int, _children) => {
  ...component,
  initialState: () => {poll: None},
  didMount: self => {
    let intervalID =
      Js.Global.setInterval(
        () =>
          PollService.getPoll(id, maybePoll =>
            self.send(SetPoll(maybePoll))
          ),
        2000,
      );
    self.onUnmount(() => Js.Global.clearInterval(intervalID));
  },
  reducer: (action, state) =>
    switch (action) {
    | SetPoll(poll) => ReasonReact.Update({...state, poll})
    },
  render: self =>
    switch (self.state.poll) {
    | Some((poll: poll)) =>
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
    | None => ReasonReact.string("Poll not found.")
    },
};
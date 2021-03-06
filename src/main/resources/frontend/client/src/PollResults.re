open Types;

type state = {poll: option(poll)};

type action =
  | SetPoll(option(poll));

let component = ReasonReact.reducerComponent("PollResults");

let make = (~id: int, _children) => {
  ...component,
  initialState: () => {poll: None},
  didMount: self => {
    PollService.getPoll(id, maybePoll => self.send(SetPoll(maybePoll)));
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
             <li className="poll__row" key={string_of_int(answer.id)}>
               {ReasonReact.string(answer.response ++ ": ")}
               {ReasonReact.string(string_of_int(answer.count))}
             </li>
           );
      <div className="poll">
        <div className="poll__question">
          {ReasonReact.string(poll.question)}
        </div>
        <ul className="poll__row-container">
          {ReasonReact.array(Array.of_list(answers))}
        </ul>
      </div>;
    | None => <NotFound />
    },
};
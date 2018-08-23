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

let make = (~id: int, _children) => {
  ...component,
  initialState: () => {poll: None, optionId: 0},
  didMount: self =>
    PollService.getPoll(id, maybePoll => self.send(SetPoll(maybePoll))),
  reducer: (action, state) =>
    switch (action) {
    | SetPoll(poll) => ReasonReact.Update({...state, poll})
    | Vote((optionId: int)) => ReasonReact.Update({...state, optionId})
    | Submit =>
      if (state.optionId !== 0) {
        Js.Promise.(PollService.vote(state.optionId)) |> ignore;
      };
      NoUpdate;
    },
  render: self =>
    switch (self.state.poll) {
    | Some((poll: poll)) =>
      let answers =
        poll.answers
        |> List.map((answer: answer) =>
             <li key={string_of_int(answer.id)}>
               <input
                 type_="checkbox"
                 onClick=(_event => self.send(Vote(answer.id)))
                 checked={self.state.optionId == answer.id}
               />
               {ReasonReact.string(answer.response)}
             </li>
           );
      <>
        {ReasonReact.string(poll.question)}
        <ul> {ReasonReact.array(Array.of_list(answers))} </ul>
        <button onClick=(_event => self.send(Submit))>
          {ReasonReact.string("Vote")}
        </button>
      </>;
    | None => ReasonReact.string("Poll not found.")
    },
};
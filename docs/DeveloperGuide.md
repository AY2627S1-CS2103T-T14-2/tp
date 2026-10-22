---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# SupporterBook Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

---

## **Acknowledgements**

This project is based on the AddressBook-Level3 project created by the [SE-EDU initiative](https://se-education.org).

---

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

---

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The **_Architecture Diagram_** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103T-T14-2/tp/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103T-T14-2/tp/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.

- At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
- At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

- [**`UI`**](#ui-component): The UI of the App.
- [**`Logic`**](#logic-component): The command executor.
- [**`Model`**](#model-component): Holds the data of the App in memory.
- [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The _Sequence Diagram_ below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

- defines its _API_ in an `interface` with the same name as the Component.
- provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-T14-2/tp/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

`CommandBox` also keeps a `CommandHistory` of the commands entered this session, which it uses to bring back earlier commands when the user presses Up or Down. `CommandHistory` is plain Java with no JavaFX code, so its behavior is tested without starting the UI. It stays in the `UI` component because it only recalls typed text; it does not affect how commands are parsed or executed.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-T14-2/tp/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-T14-2/tp/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

- executes user commands using the `Logic` component.
- listens for changes to `Model` data so that the UI can be updated with the modified data.
- keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
- depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-T14-2/tp/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:

- When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
- All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component

**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-T14-2/tp/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />

The `Model` component,

- stores the supporter data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
- stores the `Person` objects selected by the current filter in a `FilteredList`, wrapped by a `SortedList` for optional display ordering. It exposes the sorted view as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so both filter and order changes update the UI without reordering stored data.
- stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
- does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>

### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-T14-2/tp/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,

- can save both supporter data and user preference data in JSON format, and read them back into corresponding objects.
- is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
- depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

---

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Startup data-loading warning

`MainApp#initModelManager(Storage, ReadOnlyUserPrefs)` uses the existing storage validation when loading supporter data. A valid file supplies the initial supporters; a missing file supplies the sample supporters from `SampleDataUtil#getSampleAddressBook()`. Neither case records a warning.

Some sample supporters come with dated example interactions, built with the `Person` constructor that takes an interaction list. A new user can therefore try `view` and see a populated history before logging anything.

When `Storage#readAddressBook()` throws `DataLoadingException`, `MainApp` starts with an empty `AddressBook` and records a warning containing the data file path, the empty-list fallback and advice to copy the file before another command overwrites it. Startup only reads supporter data, so the unreadable file remains untouched.

`MainApp` passes this initial feedback to `UiManager`, which supplies it to `MainWindow#fillInnerParts(String)`. The warning is set after `ResultDisplay` is created, on the JavaFX application thread. The result display wraps long messages. Existing UI entry points default to empty feedback so normal launches retain their previous behavior.

Later command results and errors replace the warning through the existing command execution path. `LogicManager` continues to save after a command executes successfully, including `list`; a parse or command failure leaves the data file untouched. No storage validators, supporter fields or save rules are changed by the warning.

`MainAppTest` covers valid, empty, missing, malformed, invalid and duplicate data files, verifies that loading preserves the file's bytes, and checks that a subsequent successful command saves normally. UI testing should also verify the initial warning and replacement by command feedback using the scenarios in the manual testing appendix.

### Finding and listing supporters

`FindCommandParser` reads name keywords from the preamble and uses `ArgumentTokenizer` to read
repeatable `o/`, `t/`, `st/`, `p/` and `e/` filters. Name keywords must precede the first prefix;
each prefix value extends up to the next recognized prefix. Whitespace is normalized before
tokenization, allowing multiword organisation values and whitespace between prefixes.

The parser rejects missing criteria and empty prefix values. Stage values use
`ParserUtil#parseStage(String)` so invalid or partial stage names produce the existing stage error.
Phone queries must contain digits after removing plus signs, spaces and hyphens, and may not
contain other characters. They are search fragments rather than complete `Phone` values, so the
phone-number length constraints do not apply to queries.

`PersonMatchesSearchPredicate` defensively copies its criteria and normalizes text with
`Locale.ROOT`. Names, organisations, tags and emails use literal, case-insensitive substring
matching; stages match exactly. `normalizePhone(String)` removes formatting from both phone
criteria and stored numbers before substring matching. Missing optional fields do not satisfy
criteria on those fields.

Alternatives within a field use OR and different field groups use AND. For example,
`find tan lim o/DBS o/OCBC st/giving` requires any name keyword, any organisation alternative and
the Giving stage to match. Name-only searches retain the existing partial-match OR behavior.

`FindCommand#execute(Model)` replaces the current filter using
`Model#updateFilteredPersonList(Predicate)`. The filtered list preserves the underlying insertion
order, and the UI numbers its results from 1. `ListCommand#execute(Model)` resets the predicate
to `Model.PREDICATE_SHOW_ALL_PERSONS`, restoring the full list without changing stored supporters.

Searching leaves the viewed supporter unchanged and does not mark address-book data as changed.
`LogicManager` therefore creates no undo point and retains any redo history. Parser and predicate
unit tests cover the criteria; `FindCommandIntegrationTest` exercises filtering, displayed
indices, stored data, selection and undo/redo through `LogicManager`.

### Finding quiet supporters

`QuietCommandParser` accepts no arguments for the default 60-day threshold, or exactly one positive
integer up to `Integer.MAX_VALUE`. Invalid input produces the usage message without executing a
command. `AddressBookParser` dispatches the `quiet` command to this parser.

`QuietCommand` obtains today's local date from its `Clock` at execution. Tests inject a fixed clock.
Its predicate excludes `Stage.DECLINED` and includes a supporter when
`Person#getLastInteractionDate()` is empty or the elapsed calendar days exceed the threshold.
Counting elapsed days with `ChronoUnit.DAYS.between` avoids date-subtraction overflow for large
thresholds. A contact exactly DAYS days ago does not qualify. The captured date stays fixed until
another quiet command is run; changes to supporter data still reevaluate the predicate.

`ModelManager` wraps its `FilteredList<Person>` in a `SortedList<Person>` and exposes that same
observable sorted view through `getFilteredPersonList()`. The quiet command calls
`updateSortedPersonList(Predicate, Comparator)`, ordering absent dates first and then latest-contact
dates ascending. An explicit tie-breaker reads each supporter's current full-list position, so equal
dates retain source order even when a tag or other update replaces a supporter record. The underlying
`VersionedAddressBook` list is unchanged, so display sorting changes neither JSON order nor undo history.

`updateFilteredPersonList(Predicate)` clears the comparator as well as updating the predicate.
Existing `list`, `find`, `add`, `edit`, `undo` and `redo` paths therefore restore normal ordering
without command-specific reset logic. Index-based commands already use `getFilteredPersonList()`
and consequently operate on the displayed quiet indices. Replacing an immutable `Person` after a
log, unlog or stage change updates both filtering and sorting automatically.

`QuietCommandTest` covers boundaries, exclusions, missing interactions, ties and backdated history.
`QuietCommandIntegrationTest` checks indexed commands, stored order, selection and undo/redo. It also
verifies tied ordering after tag changes and after an interaction update creates an equal-date group.

### Deleting a supporter and their history

`DeleteCommand#execute(Model)` validates its index against `Model#getFilteredPersonList()`, so deletion after `find` targets the displayed supporter rather than the same index in the full list. It reads `Person#getInteractions().size()` before calling `Model#deletePerson(Person)` and reports that count on the second line of its success message. Every history entry is counted, including identical interactions.

Interactions are stored within each immutable `Person`. Removing that supporter from the `AddressBook` removes their history from stored data without a separate interaction-deletion loop. `Messages#format(Person)` supplies the deleted supporter's details, preserving the shared formatting of stages and optional fields.

The command retains the existing filter. `ModelManager#deletePerson(Person)` clears the viewed selection when that supporter is removed and preserves it when another supporter is deleted. After execution, `MainWindow` refreshes the detail panel from this model state. Deleting the displayed supporter removes their details and history and restores the startup prompt.

`LogicManager` saves the resulting supporter data after successful execution. Other supporters and their histories remain unchanged, including plain-text notes mentioning the deleted supporter. Failed parsing or index validation leaves data, filtering, viewed selection and the saved file unchanged.

`DeleteCommandTest` checks zero, one, multiple and duplicate interaction counts and selection behavior. `DeleteCommandIntegrationTest` runs `find`, `view` and `delete` through the logic layer, verifies the remaining filter and selection, and reloads saved JSON to check that only the targeted supporter and their history were removed.

### Duplicate supporters

Two supporters count as the same supporter if their names match, ignoring case and extra spaces. No other field is compared.

- The `Name` constructor removes leading and trailing spaces and reduces each run of spaces inside the name to one, so every `Name` is stored in a normalised form.
- `Name#isSameName(Name)` compares two names ignoring case. Because both names are already normalised, extra spaces need no special handling.
- `Person#isSamePerson(Person)` uses `Name#isSameName(Name)`. `UniquePersonList`, `AddCommand`, `EditCommand` and the loading of the data file all rely on `Person#isSamePerson(Person)`, so they all apply the same rule.
- When `AddCommand` or `EditCommand` finds a duplicate, `Messages#getErrorMessageForDuplicatePerson(List, Person)` names the supporter already in the list, and suggests adding a distinguishing word to the new name.

#### Design considerations:

**Aspect: Which fields decide whether two supporters are the same**

- **Alternative 1 (current choice):** The name only, ignoring case and extra spaces.
  - Pros: Re-adding someone already in the list is caught even if their phone or email has changed, which would otherwise split one relationship's history across two records.
  - Cons: Two different people with the same name cannot both be stored under that exact name; the user must add a distinguishing word to one of them.

- **Alternative 2:** The name together with the phone number or email.
  - Pros: Namesakes can be stored without changing either name.
  - Cons: The same person could be added twice as soon as they change jobs and contact details.

### Cultivation stage field

Every `Person` has exactly one cultivation stage (see the [Glossary](#glossary) for what each stage means). The stage is represented by the `Stage` enumeration in the `Model` component, with the six values `PROSPECT`, `CONTACTED`, `CULTIVATING`, `GIVING`, `LAPSED` and `DECLINED`.

- `Stage#isValidStage(String)` accepts any of the six stage names, ignoring case and surrounding whitespace, and `Stage#fromString(String)` converts such a string into a `Stage`.
- `Stage#toString()` returns the stage with a capital first letter (e.g. `Giving`). This is the form that is saved to the data file and shown to the user.
- `Stage.DEFAULT_STAGE` is `PROSPECT`. The `Person` constructor without a stage parameter uses it, so code that creates a `Person` without knowing about stages still works.
- `EditCommand` copies the existing stage into the edited `Person`, so editing other details never resets a supporter's stage.

In the `Storage` component, `JsonAdaptedPerson` saves the stage as a `"stage"` string. When the data file is loaded, a missing or unrecognised stage makes the whole file invalid, in the same way as any other invalid field, so the app starts with an empty list rather than silently guessing a stage.

In tests, `PersonBuilder` gives every `Person` the default stage unless `PersonBuilder#withStage(String)` is called.

#### Design considerations:

**Aspect: How to represent a stage**

- **Alternative 1 (current choice):** An enumeration of the six stages.
  - Pros: Only valid stages can exist, and later features that filter or count by stage can compare values directly.
  - Cons: Adding a new stage needs a code change.

- **Alternative 2:** A free-text field, validated like `Address`.
  - Pros: Users could invent their own stages.
  - Cons: Typos such as `givng` would create stages that no filter or count would match.

**Aspect: What to do when a saved stage is missing**

- **Alternative 1 (current choice):** Treat the data file as invalid.
  - Pros: Consistent with every other required field; a corrupted file is noticed instead of silently changing supporters' stages.
  - Cons: A data file saved before stages existed cannot be loaded.

- **Alternative 2:** Load the supporter at the default stage.
  - Pros: Old data files still load.
  - Cons: A supporter who was `Giving` could silently become a `Prospect` if the field was deleted by mistake.

### Setting and changing a stage

A stage can be set when a supporter is added, and changed afterwards with the `stage` command.

**Setting a stage in `add`:** `AddCommandParser` accepts an optional `st/STAGE` prefix (`CliSyntax.PREFIX_STAGE`) and parses it with `ParserUtil#parseStage(String)`. If the prefix is absent, `Stage.DEFAULT_STAGE` is used. The prefix is `st/` rather than `s/`, so that a name containing `s/o` can later be allowed without being misread as a stage.

**Changing a stage with `stage INDEX STAGE`:** The command is implemented by `StageCommandParser` and `StageCommand`, and works as follows.

1. `AddressBookParser` passes the arguments after the command word to `StageCommandParser#parse(String)`.
2. `StageCommandParser` splits the arguments on whitespace. Anything other than exactly two parts gives an invalid command format error. The first part is parsed with `ParserUtil#parseIndex(String)` (also an invalid command format error if it fails), and the second with `ParserUtil#parseStage(String)` (which gives the stage error message if it fails).
3. `StageCommand#execute(Model)` checks the index against the filtered list from `Model#getFilteredPersonList()`.
4. If the supporter is already at the given stage, it returns a normal `CommandResult` saying that nothing was changed. Otherwise, it creates a copy of the `Person` with the new stage and calls `Model#setPerson(Person, Person)`.

`StageCommand` does not call `Model#updateFilteredPersonList(Predicate)`, so a `find` filter stays in place after the stage changes.

The stage is also shown in the UI: `PersonCard` displays it as a badge next to the name, and adds a style class such as `stage_giving` so that each stage has its own color in `DarkTheme.css`. `Messages#format(Person)` includes the stage in command result messages.

#### Design considerations:

**Aspect: How the user changes a stage**

- **Alternative 1 (current choice):** A separate `stage INDEX STAGE` command with no prefix.
  - Pros: Changing a stage after a meeting is a frequent action, and this is the shortest command for it.
  - Cons: One more command to learn.

- **Alternative 2:** An `st/` prefix in `edit`.
  - Pros: No new command.
  - Cons: Longer to type, and mixes a frequent workflow action with occasional corrections to contact details.

**Aspect: Setting the stage a supporter is already at**

- **Alternative 1 (current choice):** Show a normal message saying nothing was changed.
  - Pros: The user's intention is already true, so it is not treated as a mistake.
  - Cons: A typo in the index that happens to hit a supporter at that stage is not flagged as an error.

- **Alternative 2:** Show an error.
  - Pros: Draws attention to a possibly mistaken command.
  - Cons: Treats a harmless command as a failure.

### Organisation field

A `Person` may belong to an organisation, such as the company a CSR contact speaks for. Individual donors usually have none, so the field is optional. It is represented by the `Organisation` class in the `Model` component.

- `Organisation#isValidOrganisation(String)` accepts any characters, because company names often contain symbols (e.g. `Procter & Gamble`). After leading and trailing spaces are removed and each run of spaces inside is reduced to one, the value must not be blank and must be at most `Organisation.MAX_LENGTH` (100) characters long.
- The `Organisation` constructor applies the same space normalisation before storing the value, so `DBS  Bank` and `DBS Bank` are equal.
- `Person` stores no organisation as `null` internally, but `Person#getOrganisation()` returns an `Optional<Organisation>`, so callers must handle the missing case explicitly. The `Person` constructors without an organisation parameter create a supporter with none.
- `EditCommand` and `StageCommand` copy the existing organisation into the `Person` they create, so editing other details or changing the stage never removes it.

In the `Storage` component, `JsonAdaptedPerson` saves the organisation as an `"organisation"` string, and leaves the key out when the supporter has none. When the data file is loaded, a missing `"organisation"` means the supporter has no organisation, while a value that breaks the rule above makes the whole file invalid.

In tests, `PersonBuilder` builds a `Person` with no organisation unless `PersonBuilder#withOrganisation(String)` is called.

**Setting an organisation:** `AddCommandParser` and `EditCommandParser` accept an optional `o/ORGANISATION` prefix (`CliSyntax.PREFIX_ORGANISATION`) and parse it with `ParserUtil#parseOrganisation(String)`, which gives the organisation error message for a blank or overlong value. In `add`, leaving out the prefix creates a supporter with no organisation. In `edit`, `EditPersonDescriptor` holds the new organisation, and leaving out the prefix keeps the existing one. An `o/` with nothing after it is not parsed as an organisation: `EditCommandParser` calls `EditPersonDescriptor#clearOrganisation()` instead, which removes the supporter's organisation. Giving `o/` more than once is reported as a repeated prefix, as for the other single-valued fields.

**Showing an organisation:** `PersonCard` shows the organisation under the supporter's name, or `(no organisation)` when there is none. `Messages#format(Person)` adds `Organisation: ...` to command result messages only when the supporter has one.

#### Design considerations:

**Aspect: How to represent a missing organisation**

- **Alternative 1 (current choice):** `Person#getOrganisation()` returns an `Optional<Organisation>`.
  - Pros: The type shows that the value may be missing, so code that forgets to handle it does not compile.
  - Cons: Callers need an extra step (e.g. `map` or `isPresent`) to read the value.

- **Alternative 2:** An `Organisation` object holding an empty string when there is none.
  - Pros: Callers can always read `value` directly.
  - Cons: An empty organisation would be a valid object, which goes against the rule that an organisation is never blank, and every caller would have to remember to check for it.

**Aspect: What to do when a saved organisation is missing**

- **Alternative 1 (current choice):** Load the supporter with no organisation.
  - Pros: Matches the field being optional, and data files saved before organisations existed still load.
  - Cons: An organisation deleted from the file by mistake is not noticed.

- **Alternative 2:** Treat the data file as invalid, as for a missing stage.
  - Pros: Consistent with the required fields.
  - Cons: Every supporter without an organisation would need an explicit empty value in the file.

### Interaction history field

Each `Person` owns an interaction history. An entry is represented by the immutable `Interaction` class in the `Model` component and contains a `LocalDate` and a short note.

- An interaction note is trimmed before it is stored. It must not be blank after trimming and must be at most `Interaction.MAX_NOTE_LENGTH` (500) characters long.
- `Person` stores interactions in logging order. Its constructor makes a defensive copy of the supplied list, and `Person#getInteractions()` returns an unmodifiable view.
- Existing `Person` constructors create an empty interaction history. This keeps existing creation paths compatible with the new field.
- `EditCommand` and `StageCommand` copy the existing history into the replacement `Person`, so changing other supporter details never removes past interactions.
- Interaction history is included in `Person#equals(Object)` and `Person#hashCode()`, but not in `Person#isSamePerson(Person)`. Supporter identity therefore remains name-based.

In the `Storage` component, `JsonAdaptedInteraction` stores a date as an ISO `yyyy-MM-dd` string and a note as a string. `JsonAdaptedPerson` stores the entries in an `"interactions"` array while preserving their order. When loading, a missing array means the supporter has an empty history, so data files created before interaction histories existed remain compatible. A missing interaction field, malformed date, blank note or note longer than 500 characters makes the data file invalid.

In tests, `PersonBuilder` builds a `Person` with an empty history unless `PersonBuilder#withInteractions(Interaction...)` is called.

#### Design considerations:

**Aspect: Where to store interaction histories**

- **Alternative 1 (current choice):** Each `Person` owns a list of `Interaction` objects.
  - Pros: A supporter's details and relationship history form one record, so replacing or deleting that supporter naturally handles the history with it.
  - Cons: Operations across every interaction must traverse all supporters.

- **Alternative 2:** Store all interactions in one address-book-level collection and separately associate each one with a supporter.
  - Pros: Whole-address-book interaction queries operate on one collection.
  - Cons: Supporter replacement and deletion require maintaining a separate relationship and avoiding orphaned interactions.

**Aspect: How to represent a supporter with no interactions**

- **Alternative 1 (current choice):** Use an empty list and treat a missing `"interactions"` array as empty when loading.
  - Pros: Callers can iterate without handling a second absent state, and older data files load without migration.
  - Cons: An array deleted accidentally is indistinguishable from a supporter who has no history.

- **Alternative 2:** Use an `Optional<List<Interaction>>` and reject a missing array.
  - Pros: Absence can be distinguished from an explicitly empty history.
  - Cons: It adds a redundant state and prevents older data files from loading.

### Logging an interaction

**Parsing `log INDEX nt/NOTE [dt/DATE]`:** The command is parsed by `LogCommandParser` as follows.

1. `AddressBookParser` passes the arguments after the command word to `LogCommandParser#parse(String)`.
2. The parser requires `INDEX` and `nt/NOTE`, and accepts optional `dt/DATE` in either prefix order. It parses the index with `ParserUtil#parseIndex(String)`, reporting an invalid command format if it fails, then rejects repeated prefixes.
3. It validates the note with `ParserUtil#parseInteractionNote(String)` and any supplied date with `ParserUtil#parseInteractionDate(String)`. Dates must be valid calendar dates in `yyyy-MM-dd` format, from `1900-01-01` through today (inclusive), using the computer's clock. The lower bound allows historical records while rejecting implausibly old dates and likely year-entry mistakes.
4. It creates a `LogCommand` with the parsed index, note and an `Optional<LocalDate>`. An omitted date remains empty until execution.

`LogCommand` performs the model update as follows.

1. `LogCommand#execute(Model)` obtains the currently displayed supporters from `Model#getFilteredPersonList()` and checks the given index against that list. An out-of-range index gives the same invalid-index error as the other index-based commands.
2. It creates an `Interaction` using the supplied date, or today's date at execution if none was supplied. The default uses the computer's clock; tests can supply a fixed `Clock`.
3. It copies the supporter's existing interactions in logging order and appends the new interaction. The new entry's zero-based stored index is the updated list size minus one.
4. It creates a replacement `Person` with the updated history and copies every other field unchanged, including the cultivation stage and tags. It then calls `Model#setPerson(Person, Person)` and selects the updated supporter with `Model#setViewedPerson(Person)`.
5. It finds the new entry's position in `Person#getInteractionIndicesInDisplayOrder()`. Subtracting that zero-based display position from the history size gives its chronological number: oldest is `#1`, newest is largest. The returned `CommandResult` reports this number, supporter, date and note.

Identical interactions are appended normally because two calls or messages with the same note can happen on the same day. `LogCommand` also does not call `Model#updateFilteredPersonList(Predicate)`, so logging against a supporter in a `find` result keeps that filter in place.

#### Design considerations:

**Aspect: How to update a supporter's interaction history**

- **Alternative 1 (current choice):** Create a replacement `Person` containing a copied and extended interaction list.
  - Pros: `Person` and its interaction history remain immutable to callers, and the update follows the same `Model#setPerson(Person, Person)` pattern as `edit` and `stage`.
  - Cons: Every field must be copied correctly even though only the history changes.

- **Alternative 2:** Mutate the interaction list inside the existing `Person`.
  - Pros: Only the new interaction needs to be added.
  - Cons: It breaks `Person`'s immutability and can bypass the observable-list replacement that informs other components of a changed supporter.

**Aspect: How to obtain today's date when `dt/` is omitted**

- **Alternative 1 (current choice):** Store a `Clock` in `LogCommand`, using the system-default clock in production and a fixed clock in tests.
  - Pros: The command follows the computer's date in normal use while date-dependent tests remain deterministic.
  - Cons: The command carries an extra dependency used only to obtain the date.

- **Alternative 2:** Call `LocalDate#now()` directly during execution.
  - Pros: Less constructor and field code.
  - Cons: Tests can fail or require changing expected values depending on the day they run.

### Editing an interaction

**Parsing `editlog INDEX ENTRY [nt/NOTE] [dt/DATE]`:** The command is parsed by `EditLogCommandParser` as follows.

1. `AddressBookParser` passes the arguments to `EditLogCommandParser#parse(String)`, which tokenizes `nt/` and `dt/`.
2. Parse the preamble with `ParserUtil#parseInteractionIndices(String)`, reporting malformed indices with the command's usage message, then reject duplicate prefixes.
3. Require at least one edit field. Validate supplied values using `ParserUtil#parseInteractionNote(String)` and `ParserUtil#parseInteractionDate(String)`. The existing note constraints and date range of `1900-01-01` through today apply. Note validation precedes date validation regardless of prefix order.
4. Create an `EditLogCommand` with the two indices, an `Optional<String>` note and an `Optional<LocalDate>` date. Empty optionals represent omitted fields, not requests to clear them.

`EditLogCommand` performs the model update as follows.

1. Validate the supporter index against `Model#getFilteredPersonList()`, then the entry number against that supporter's history size. These failures have distinct messages and leave data and selection unchanged.
2. Convert the chronological entry number to a zero-based display position using `historySize - entryNumber`. Look up that position in `Person#getInteractionIndicesInDisplayOrder()` to obtain the stored index.
3. Create a replacement `Interaction`, taking omitted fields from the original. Replace the entry at its stored index in a copied list. This distinguishes identical entries and preserves logging position for equal-date ordering.
4. Create a replacement `Person` preserving all other fields, including the introducer. Call `Model#setPerson(Person, Person)` and `Model#setViewedPerson(Person)` without changing the list filter.
5. Find the stored index in the updated display-order mapping. Subtract its display position from the history size to obtain the edited entry's new chronological number for the success message. `SupporterDetailData` uses the same ordering to refresh the panel.

Supplying unchanged values succeeds normally. Date edits can renumber other entries, but do not reorder the stored list. Last contact remains derived from the latest interaction date.

`EditLogCommandTest` checks command behavior, while parser tests cover input validation. `EditLogCommandIntegrationTest` verifies renumbering and display order from command input through detail-panel data, without starting JavaFX.

### Deleting an interaction

**Parsing `unlog INDEX ENTRY`:** `AddressBookParser` passes the arguments to `UnlogCommandParser`. It uses `ParserUtil#parseInteractionIndices(String)` to require exactly two positive integers and creates an `UnlogCommand`. Parsing failures are reported with the command's usage message.

`UnlogCommand` performs the model update as follows.

1. Validate the supporter index against `Model#getFilteredPersonList()`, then validate the entry number against that supporter's history size. These failures have distinct messages and leave data and selection unchanged.
2. Convert the chronological entry number to a zero-based display position using `historySize - entryNumber`. Look up that position in `Person#getInteractionIndicesInDisplayOrder()` to obtain the stored index.
3. Copy the interaction list and remove the entry at that stored index. Removing by position distinguishes identical interactions and preserves the remaining logging order.
4. Create a replacement `Person`, preserving all other fields, including the introducer. Call `Model#setPerson(Person, Person)` and `Model#setViewedPerson(Person)` without changing the list filter.
5. Return the removed entry's pre-deletion number, date and note. `SupporterDetailData` derives contiguous chronological numbers from the updated history and supplies the empty-history state when none remain.

`UnlogCommandTest` checks command behavior, while parser tests cover input validation. `UnlogCommandIntegrationTest` verifies renumbering, display order and empty history from command input through detail-panel data, without starting JavaFX.

### Viewing a supporter's details and interaction history

The `view INDEX` command selects a supporter from the displayed list and reports their name and number of logged interactions, or indicates that no interactions have been logged yet.

On successful index validation, `ViewCommand` records the supporter using `Model#setViewedPerson(Person)` and counts `Person#getInteractions()` for the command result. The displayed list, its filter and supporter data remain unchanged.

`SupporterDetailData` prepares the display values without a JavaFX dependency:

* Optional fields and empty tags become `-`. Tag names are sorted alphabetically and joined with commas.
* `Person#getInteractionIndicesInDisplayOrder()` returns zero-based stored indices sorted by date descending, then logging position descending. Using indices keeps identical interactions distinct without reordering stored history.
* `SupporterDetailData` follows those indices to create immutable `InteractionEntry` objects. Numbers descend from the history size to `#1`, so the oldest entry has the smallest number. On equal dates, the later-logged entry appears first and has the larger number.
* Back-dating and editing interaction dates can renumber existing entries. The panel, `LogCommand` and `EditLogCommand` use the same ordering method so displayed numbers match success messages.

`SupporterDetailPanel#setSupporter(Optional<Person>)` skips rendering when the supporter record is unchanged, preserving the scroll position through `list`, `find` and `add`. Switching supporters or changing the displayed record rebuilds the history and returns scrolling to the top.

### Viewed supporter state

`ModelManager` stores the viewed supporter as a `Person` reference, independently of the displayed list. `Model#getViewedPerson()` returns an `Optional<Person>`, initially empty, and `Model#setViewedPerson(Person)` requires a non-null supporter. Selection is kept in memory and starts empty each time the app launches.

Selection follows these rules:

* `list`, `find` and `add` retain the viewed supporter, even if they are no longer displayed.
* Successful `view`, `log`, `editlog`, `unlog` and `stage` commands select their target. `log`, `editlog` and `unlog` select the replacement record after updating its history. `stage` selects the replacement record after a stage change, or the existing record when the requested stage is already set.
* `ModelManager#setPerson(Person, Person)` updates selection to the replacement record when the viewed supporter is changed.
* `ModelManager#deletePerson(Person)` clears selection when the viewed supporter is deleted.
* `ModelManager#setAddressBook(ReadOnlyAddressBook)` clears selection when all supporter data is replaced, including by `clear`.

Editing or deleting another supporter preserves selection. Parsing and index-validation failures also preserve it.

#### Design considerations:

**Aspect: How to record the viewed supporter**

* **Alternative 1 (current choice):** Store a `Person` reference in `ModelManager`.
  * Pros: Selection remains associated with the same supporter when the displayed list changes.
  * Cons: Replacing or deleting that supporter requires updating the stored reference.

* **Alternative 2:** Store the supporter's displayed index.
  * Pros: Uses the index supplied by the command directly.
  * Cons: A filter or list change can make that index refer to another supporter, or to no supporter at all.

**Aspect: Where to maintain selection after a supporter changes**

* **Alternative 1 (current choice):** Update selection in the model's replacement, deletion and reset methods.
  * Pros: Every command using those methods keeps selection consistent, including when a supporter's name changes.
  * Cons: Those model methods also need to manage selection state.

* **Alternative 2:** Update selection separately in each command that changes supporters.
  * Pros: Each command controls its own selection behavior.
  * Cons: The same logic is repeated across commands, and a new command can leave selection pointing to an outdated record.

### Optional contact fields

A supporter's phone number, email and address are optional, but every supporter has at least one of a phone number and an email, so there is always a way to reach them.

- `Person` stores a missing field as `null` internally, and `Person#getPhone()`, `Person#getEmail()` and `Person#getAddress()` return an `Optional`, in the same way as `Person#getOrganisation()`.
- The main `Person` constructor takes each optional field as an `Optional`, and throws an `IllegalArgumentException` if both the phone number and the email are empty. `Person#isValidContact(Optional, Optional)` holds this rule so that other components can check it first.
- `AddCommandParser` requires only `n/NAME`. If neither `p/PHONE` nor `e/EMAIL` is given, it reports `AddCommand.MESSAGE_MISSING_CONTACT`, which names the two prefixes so the user knows how to fix the command.
- `EditCommand` keeps a field's current value, which may be empty, when the edit leaves it out. A prefix with nothing after it (`p/`, `e/`, `a/` or `o/`) clears that field: `EditCommandParser` marks it with `EditPersonDescriptor#clearPhone()` and the like. Before creating the edited `Person`, `EditCommand` checks `Person#isValidContact(Optional, Optional)` and reports `EditCommand.MESSAGE_NO_CONTACT_LEFT` if the edit would leave neither a phone number nor an email.
- `JsonAdaptedPerson` leaves out the keys of missing fields when saving. When loading, a missing key means the field is empty, and a supporter with neither a phone number nor an email makes the whole data file invalid.
- `Messages#format(Person)` and `PersonCard` show only the fields a supporter has. `PersonCard` hides the label of a missing field, so it takes up no space.

#### Design considerations:

**Aspect: Which contact details are required**

- **Alternative 1 (current choice):** At least one of a phone number and an email.
  - Pros: Online donors who give only an email, and people met at events who give only a number, can both be recorded, while a supporter the user has no way to reach is still rejected.
  - Cons: The rule involves two fields, so it cannot be checked by either field's own class and needs a separate check in `Person`.

- **Alternative 2:** Both required, as in AB3.
  - Pros: Simpler; every supporter can be reached in both ways.
  - Cons: The user would have to invent a phone number or email for many real supporters.

### Undo/redo feature

#### Implementation

Undo and redo are facilitated by `VersionedAddressBook`, which extends `AddressBook` with a history of saved states. The history is stored as an `addressBookStateList` of read-only copies, together with a `currentStatePointer` giving the index of the copy that matches the current data. `VersionedAddressBook` adds the following operations:

- `VersionedAddressBook#commit()` -- Saves a copy of the current supporter data in the history, after discarding any undone states.
- `VersionedAddressBook#undo()` -- Restores the previous state from the history.
- `VersionedAddressBook#redo()` -- Restores a previously undone state from the history.
- `VersionedAddressBook#canUndo()` and `VersionedAddressBook#canRedo()` -- Check whether there is a state to move to.
- `VersionedAddressBook#hasChangesSinceLastCommit()` -- Checks whether the current data differs from the state at the `currentStatePointer`.

These operations are exposed in the `Model` interface as `Model#commitAddressBookIfChanged()`, `Model#undoAddressBook()`, `Model#redoAddressBook()`, `Model#canUndoAddressBook()` and `Model#canRedoAddressBook()`. Only `ModelManager` knows that its address book is versioned; the rest of the app, including storage, still sees a `ReadOnlyAddressBook`.

Each saved state is a separate `AddressBook` created with the copy constructor, so later changes to the current data never alter it. The copies share `Person` objects, which is safe because `Person` is immutable: `edit`, `log`, `stage`, `intro`, `tag` and `untag` replace a supporter with a new `Person` rather than changing the old one. A deleted supporter's interactions are stored inside their `Person`, so undoing `delete` restores the interaction history too. Deleting a supporter also removes them as the introducer of other supporters. Since a state is the whole supporter data, one undo brings back the deleted supporter and those introductions together.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application. The `VersionedAddressBook` is initialized with a copy of the loaded supporter data as its only state, and the `currentStatePointer` points to that state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 2` to delete the 2nd supporter in the displayed list. After the command succeeds, `LogicManager` calls `Model#commitAddressBookIfChanged()`, which saves the data as it is after the deletion into the `addressBookStateList` and shifts the `currentStatePointer` to the new state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `log 1 nt/Called` to log an interaction with the 1st supporter. This also changes the data, so `LogicManager` commits again, causing another state to be saved into the `addressBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** Commands do not commit themselves. `ModelManager` marks the data as changed whenever `addPerson`, `setPerson`, `deletePerson` or `setAddressBook` is called, and `Model#commitAddressBookIfChanged()` saves a state only if that mark is set and `VersionedAddressBook#hasChangesSinceLastCommit()` confirms the data really differs, then clears the mark. So every command that changes supporter data saves exactly one state, even one that changes several supporters (such as deleting an introducer). A command that fails saves none. Neither does one that changes nothing, such as `stage` with the current stage, `tag` with a tag the supporter already has, `edit` with the values the supporter already has, or `clear` on an empty list. Such a command therefore also keeps any undone states available for `redo`.
</box>

Step 4. The user decides that logging the interaction was a mistake, and executes `undo`. `UndoCommand` calls `Model#undoAddressBook()`, which shifts the `currentStatePointer` once to the left and copies that state back into the current data with `AddressBook#resetData()`.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index 0, pointing to the state the app started with, then there is no earlier state to restore. `UndoCommand` uses `Model#canUndoAddressBook()` to check if this is the case. If so, it shows an error to the user rather than attempting to perform the undo.
</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</box>

After restoring the data, `UndoCommand` shows all supporters, so that a restored supporter is visible even if a `find` filter was active. Restoring a state does not mark the data as changed, so `Model#commitAddressBookIfChanged()` saves no new state for `undo` itself. `LogicManager` then saves the restored data to the data file, as it does after every successful command.

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The supporter shown in the detail panel is not part of the saved states. After restoring a state, `ModelManager` replaces the viewed supporter with the supporter of the same name in the restored data, or clears the detail panel if there is none.

The `redo` command does the opposite: it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the data to that state.

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest state, then there are no undone states to restore. `RedoCommand` uses `Model#canRedoAddressBook()` to check if this is the case. If so, it shows an error to the user rather than attempting to perform the redo.
</box>

Step 5. The user then executes `list`. Commands such as `list`, `find` and `view` do not change supporter data, so `Model#commitAddressBookIfChanged()` saves nothing for them. Thus, the `addressBookStateList` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `clear`, which changes the data, so a new state is committed. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all states after the `currentStatePointer` are purged. Reason: It no longer makes sense to redo the `log 1 nt/Called` command. This is the behavior that most modern desktop applications follow.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

The history is kept in memory only, so it starts again from the loaded data each time the app is launched.

#### Design considerations:

**Aspect: How undo & redo execute:**

- **Alternative 1 (current choice):** Saves a copy of the entire supporter data after each change.
  - Pros: Easy to implement, and one mechanism works for every command, including `clear`.
  - Cons: May have performance issues in terms of memory usage. With the few hundred supporters this product targets, and copies that share immutable `Person` objects, this cost is small.

- **Alternative 2:** Individual command knows how to undo/redo by itself (the Command pattern with an `undo()` method on each command).
  - Pros: Will use less memory (e.g. for `delete`, just save the supporter being deleted).
  - Cons: We must ensure that the implementation of each individual command is correct. For example, undoing `delete` must put the supporter back at the same position with all their interactions.

**Aspect: Where states are committed:**

- **Alternative 1 (current choice):** `ModelManager` marks the data as changed in `addPerson`, `setPerson`, `deletePerson` and `setAddressBook`, and `LogicManager` calls `Model#commitAddressBookIfChanged()` after every successful command.
  - Pros: Commands that change data, such as `intro`, `tag` and `untag`, and later ones such as `editlog` and `unlog`, are undoable without any extra code, since every data change goes through those four methods. Commands that change nothing, such as `list` or a `stage` command that leaves the stage unchanged, create no undo step.
  - Cons: A new `Model` method that changes data must also mark the data as changed. `UndoableCommandsTest` checks each data-changing command through `LogicManager`.

- **Alternative 2:** Each command that changes data calls a commit method itself.
  - Pros: The commit is visible in each command's code.
  - Cons: Every new command must remember to commit. Forgetting gives no error; undo just skips that command or reverts it together with the next one.

- **Alternative 3:** `LogicManager` commits after every successful command, without checking for changes.
  - Pros: Simplest; no flag is needed.
  - Cons: Read-only commands would create empty undo steps, so `undo` would sometimes appear to do nothing.

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_

---

## **Documentation, logging, testing, dev-ops**

- [Documentation guide](Documentation.md)
- [Testing guide](Testing.md)
- [Logging guide](Logging.md)
- [DevOps guide](DevOps.md)

---

## **Appendix: Requirements**

### Product scope

**Target user profile**:

- Donor and corporate-partnership executives at small (Tier 1) charities, who single-handedly manage every supporter relationship without access to a CRM.

**Value proposition**: SupporterBook helps the sole fundraising and outreach staff at a small charity stay on top of every donor and corporate relationship, keeping each supporter’s history and the introductions behind it in one place, so follow-ups happen on time and relationships outlast whoever holds the role. It also allows fast CLI-based retrieval.

### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (later version) - `*`

Stories are grouped by epic, with the highest-priority stories listed first within each epic.

#### A. Getting started

| Priority | As a …                                     | I want to …                                                  | So that …                                                         |
| -------- | ------------------------------------------ | ------------------------------------------------------------ | ----------------------------------------------------------------- |
| `*`      | potential user exploring the app           | see the app pre-populated with sample supporter records      | I can tell what it will look like once my own data is in it       |
| `*`      | potential user exploring the app           | see a list of all available commands with an example of each | I can learn what the app does without reading a manual            |
| `*`      | user ready to start using the app for real | clear all sample data in one command                         | I can begin from a clean list of my own supporters                |
| `*`      | new user who already keeps a spreadsheet   | import my existing supporter list from a CSV file            | I do not have to retype 250 contacts before the app is of any use |
| `*`      | new user                                   | see where the app stores my data file                        | I know what to back up and what to hand over                      |

#### B. Contact records

| Priority | As a …                                      | I want to …                                                              | So that …                                                            |
| -------- | ------------------------------------------- | ------------------------------------------------------------------------ | -------------------------------------------------------------------- |
| `* * *`  | fundraiser                                  | add a supporter with their name and contact details                      | every supporter sits in one place instead of in my inbox             |
| `* * *`  | fundraiser                                  | see a list of all my supporters                                          | I can see everyone I am responsible for                              |
| `* * *`  | fundraiser                                  | delete a supporter                                                       | my list does not fill up with people I will never contact again      |
| `* * *`  | fundraiser                                  | edit a supporter's details                                               | their phone and email stay current when they change jobs             |
| `* * *`  | fundraiser                                  | record the organisation a contact belongs to                             | I know which company a CSR contact speaks for                        |
| `* *`    | fundraiser                                  | view one supporter's whole record on a single screen                     | I can prepare for a meeting in the minute before it starts           |
| `* *`    | fundraiser                                  | add free-form notes to a supporter                                       | I can keep the things that do not fit any field                      |
| `* *`    | fundraiser recording Singaporean names      | save names containing hyphens, apostrophes, s/o and d/o                  | I can record my supporters' names as they actually are               |
| `* *`    | fundraiser with overseas corporate contacts | save phone numbers that include a country code                           | I can store the numbers I actually dial                              |
| `* *`    | fundraiser                                  | mark whether a contact is an individual supporter or a corporate contact | I can tell the two kinds of relationship apart                       |
| `*`      | fundraiser                                  | record a contact's job title                                             | I know whether I am talking to someone who can approve a partnership |
| `*`      | fundraiser                                  | record how a supporter prefers to be addressed                           | I do not open a letter to a major donor with the wrong name          |

#### C. Finding and retrieving

| Priority | As a …         | I want to …                                                                 | So that …                                                                    |
| -------- | -------------- | --------------------------------------------------------------------------- | ---------------------------------------------------------------------------- |
| `* * *`  | fundraiser     | find a supporter by part of their name                                      | I can pull up a record without remembering the exact spelling                |
| `*`      | fundraiser     | search on fields other than the name                                        | I can find the person at a particular bank when their name escapes me        |
| `*`      | forgetful user | be warned when I add someone whose name closely matches an existing contact | I do not create a second record for a person I already have                  |
| `*`      | fundraiser     | filter my supporters by cultivation stage                                   | I can work through everyone at one stage of the relationship                 |
| `*`      | fundraiser     | combine more than one filter in a single search                             | I can ask a precise question such as corporate contacts untouched since June |
| `*`      | fundraiser     | sort my list by the date I last spoke to each person                        | whoever has gone quiet rises to the top                                      |
| `*`      | fundraiser     | see how many supporters match a search                                      | I know the size of the job before I start working through it                 |

#### D. Interaction history

| Priority | As a …     | I want to …                                                                   | So that …                                             |
| -------- | ---------- | ----------------------------------------------------------------------------- | ----------------------------------------------------- |
| `* * *`  | fundraiser | log an interaction against a supporter with a date and a short note           | what was said stops living only in my memory          |
| `* * *`  | fundraiser | see all logged interactions for a supporter in date order                     | I can see how the relationship has developed          |
| `* *`    | fundraiser | correct or remove a logged interaction                                        | a mistyped date does not stay wrong forever           |
| `* *`    | fundraiser | see the date of the most recent interaction beside each supporter in the list | I can see at a glance who has gone quiet              |
| `*`      | fundraiser | log an interaction dated in the past                                          | I can write up a meeting the week after it happened   |
| `*`      | fundraiser | record what kind of interaction it was, such as a call, a meeting or an email | I can tell a coffee meeting apart from a mass mailing |
| `*`      | fundraiser | record the next step I intend to take with a supporter                        | I know what I promised to do next                     |
| `*`      | fundraiser | see which intended next steps are overdue                                     | nothing I promised slips silently                     |

#### E. Introductions and relationships

| Priority | As a …     | I want to …                                                         | So that …                                             |
| -------- | ---------- | ------------------------------------------------------------------- | ----------------------------------------------------- |
| `*`      | fundraiser | record who introduced me to a supporter                             | I know whose goodwill created the relationship        |
| `*`      | fundraiser | see everyone a given person has introduced me to                    | I can tell which board member opens the most doors    |
| `*`      | fundraiser | trace an introduction chain back to where it started                | I can see how a major partnership actually came about |
| `*`      | fundraiser | see which introductions I have not yet reported back on             | the person who made them keeps making them            |
| `*`      | fundraiser | link contacts who share an affiliation such as a company or a board | I can see everyone I know at one organisation         |

#### F. Cultivation and prioritisation

| Priority | As a …     | I want to …                                                                     | So that …                                                                          |
| -------- | ---------- | ------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------- |
| `* * *`  | fundraiser | set a cultivation stage on a supporter                                          | I know whether they have been approached, are giving, have lapsed or have declined |
| `* * *`  | fundraiser | change a supporter's stage in a single command                                  | updating where things stand after a meeting takes seconds                          |
| `* *`    | fundraiser | tag supporters with labels of my own                                            | I can group them in ways I did not anticipate when I started                       |
| `*`      | fundraiser | see how many supporters sit at each stage                                       | I can tell early in the year whether I have enough prospects                       |
| `*`      | fundraiser | record that a supporter declined, together with a reason                        | I do not approach them again the same way                                          |
| `*`      | fundraiser | pin a small number of key relationships                                         | the handful my year depends on stay in front of me                                 |
| `*`      | fundraiser | see supporters marked as giving who have had no interaction this financial year | lapsed givers surface before the year closes                                       |

#### G. Data safety and trust

| Priority | As a …                                           | I want to …                                                            | So that …                                                              |
| -------- | ------------------------------------------------ | ---------------------------------------------------------------------- | ---------------------------------------------------------------------- |
| `* * *`  | fundraiser                                       | have every change saved automatically                                  | I never lose work by forgetting to save                                |
| `* *`    | careless user                                    | undo my last command                                                   | a mistaken delete does not cost me a record I cannot rebuild           |
| `*`      | cautious user                                    | be asked to confirm before a command that wipes all my data            | one stray keystroke cannot empty the list                              |
| `*`      | fundraiser handling other people's personal data | keep everything in a file on my own machine with nothing sent anywhere | supporters' details never leave the charity's laptop                   |
| `*`      | fundraiser                                       | open and repair my data file in a text editor                          | a corrupted file does not mean starting again from nothing             |
| `*`      | fundraiser                                       | export my supporters to a spreadsheet                                  | I can hand a list to my executive director without giving them the app |

#### H. Speed for expert users

| Priority | As a …      | I want to …                                          | So that …                                                                |
| -------- | ----------- | ---------------------------------------------------- | ------------------------------------------------------------------------ |
| `*`      | expert user | bring back previous commands with the up arrow       | I can repeat a near-identical command without retyping it                |
| `*`      | expert user | use short aliases for the commands I run most often  | logging a call costs me a few keystrokes                                 |
| `*`      | expert user | see the parameters of a command as I type it         | I do not have to remember the exact format of a command I use rarely     |
| `*`      | expert user | delete a range of contacts in one command            | clearing up after a campaign does not take twenty commands               |
| `*`      | expert user | log one interaction against several contacts at once | recording a meeting with three people from one company takes one command |

#### I. Continuity and handover

| Priority | As a …                                    | I want to …                                                             | So that …                                                           |
| -------- | ----------------------------------------- | ----------------------------------------------------------------------- | ------------------------------------------------------------------- |
| `*`      | newly hired executive inheriting the role | read the interaction notes my predecessor left                          | I am not starting every relationship from zero                      |
| `*`      | departing executive                       | hand over a single data file that holds everything                      | what I know about our supporters stays with the charity             |
| `*`      | returning user after weeks on an event    | see which relationships have gone stale while I was busy                | I can pick up where I left off                                      |
| `*`      | long-time user                            | archive supporters I no longer actively cultivate without deleting them | my working list stays short while the history survives              |
| `*`      | fundraiser reporting to the board         | produce a summary of my activity over a period                          | I can show what I have been doing without rebuilding it from memory |

### Use cases

(For all use cases below, the **System** is the `SupporterBook` and the **Actor** is the `user`, unless specified otherwise)

**Use case: UC01 - Add a supporter**

**MSS**

1.  User requests to add a supporter, giving their details.
2.  SupporterBook adds the supporter and shows the full list with the new supporter at the end.

    Use case ends.

**Extensions**

- 1a. User did not provide a name.
  - 1a1. SupporterBook shows an error message.

    Use case resumes from step 1.

- 1b. User provided neither a phone number nor an email.
  - 1b1. SupporterBook shows an error message stating that at least one is required.

    Use case resumes from step 1.

- 1c. User provided a detail that is invalid (e.g. a name with symbols, a phone number with spaces, an unknown stage).
  - 1c1. SupporterBook shows an error message for that detail.

    Use case resumes from step 1.

- 1d. A supporter with the same name already exists, ignoring capitalisation and extra spaces.
  - 1d1. SupporterBook shows an error message suggesting a way to tell the two supporters apart.

    Use case resumes from step 1.

- 1e. User gives more than one value for a detail that takes only one value (e.g. two names).
  - 1e1. SupporterBook shows an error message, indicating the detail with multiple values given.

    Use case resumes from step 1.

**Use case: UC02 - List all supporters**

**MSS**

1.  User requests to list all supporters.
2.  SupporterBook shows the list of every supporter, clearing any find filter.

    Use case ends.

**Extensions**

- 2a. No supporters have been added yet.
  - 2a1. SupporterBook shows an empty list.

    Use case ends.

**Use case: UC03 - Find supporters by part of a name**

**MSS**

1.  User requests to find supporters whose name contains certain keywords, giving the keyword(s).
2.  SupporterBook shows every supporter whose name contains any keyword given, ignoring case and matching even part of a word. Results keep their original list order and replace any previous find filter.

    Use case ends.

**Extensions**

- 1a. User did not provide any keywords.
  - 1a1. SupporterBook shows an error message with the correct command format.

    Use case resumes from step 1.

- 2a. No supporter's name contains any of the keywords given.
  - 2a1. SupporterBook shows an empty list.

    Use case ends.

**Use case: UC04 - Edit a supporter**

**Preconditions: A list of supporters is shown, with an index beside each supporter**

**MSS**

1.  User requests to edit an existing supporter's details, giving an index and the field(s) to edit.
2.  SupporterBook updates only the specified field(s) of the supporter, without altering the supporter's interaction history.
3.  SupporterBook shows the list of every supporter, clearing any find filter. If the edited supporter's details were being shown, SupporterBook shows the updated details.

    Use case ends.

**Extensions**

- 1a. The given index is invalid.
  - 1a1. SupporterBook shows an error message.

    Use case resumes from step 1.

- 1b. User does not give a field.
  - 1b1. SupporterBook shows an error message, suggesting for the user to provide at least one field.

    Use case resumes from step 1.

- 1c. User gives multiple values for the same field (excluding the tags field).
  - 1c1. SupporterBook shows an error message, indicating the field with multiple values given.

    Use case resumes from step 1.

- 1d. User gives invalid values for certain fields.
  - 1d1. SupporterBook shows an error message, indicating the field with invalid value.

    Use case resumes from step 1.

- 1e. The new name is the same as another supporter's name, ignoring capitalisation and extra spaces.
  - 1e1. SupporterBook shows an error message, suggesting a way to tell the two supporters apart.

    Use case resumes from step 1.

**Use case: UC05 - Delete a supporter**

**Preconditions: A list of supporters is shown, with an index beside each supporter**

**MSS**

1.  User requests to delete a specific supporter in the list, giving an index.
2.  SupporterBook deletes the supporter, together with every interaction logged with them.
3.  SupporterBook confirms the deletion, stating how many logged interactions were deleted with the supporter.

    Use case ends.

**Extensions**

- 1a. The given index is invalid.
  - 1a1. SupporterBook shows an error message.

    Use case resumes from step 1.

- 2a. The deleted supporter's details were being shown.
  - 2a1. SupporterBook stops showing that supporter's details.

    Use case resumes from step 3.

**Use case: UC06 - Log an interaction with a supporter**

**Preconditions: A list of supporters is shown, with an index beside each supporter**

**MSS**

1.  User requests to log an interaction with a specific supporter in the list, giving an index and a short note.
2.  SupporterBook records the interaction, dated today, in the supporter's interaction history.
3.  SupporterBook confirms the logged interaction and shows the supporter's details and updated interaction history.

    Use case ends.

**Extensions**

- 1a. The given index is invalid.
  - 1a1. SupporterBook shows an error message.

    Use case resumes from step 1.

- 1b. User does not give a note.
  - 1b1. SupporterBook shows an error message.

    Use case resumes from step 1.

- 1c. The given note is blank or longer than 500 characters.
  - 1c1. SupporterBook shows an error message stating the rules for a note.

    Use case resumes from step 1.

- 1d. User gives more than one note.
  - 1d1. SupporterBook shows an error message, indicating that only one note can be given.

    Use case resumes from step 1.

**Use case: UC07 - View a supporter's details and interaction history**

**Preconditions: A list of supporters is shown, with an index beside each supporter**

**MSS**

1.  User requests to view a specific supporter in the list, giving an index.
2.  SupporterBook shows the supporter's details and interaction history, with the most recent interactions shown first.

    Use case ends.

**Extensions**

- 1a. The given index is invalid.
  - 1a1. SupporterBook shows an error message.

    Use case resumes from step 1.

- 2a. The supporter has no logged interactions.
  - 2a1. SupporterBook shows the supporter's details and indicates that there are no interactions logged yet.

    Use case ends.

**Use case: UC08 - Set a supporter's cultivation stage**

**Preconditions: A list of supporters is shown, with an index beside each supporter**

**MSS**

1.  User requests to set the cultivation stage of a specific supporter in the list.
2.  SupporterBook updates the supporter's cultivation stage.
3.  SupporterBook confirms the change and shows the updated supporter.

    Use case ends.

**Extensions**

- 1a. The given index is invalid.
  - 1a1. SupporterBook shows an error message.

    Use case resumes from step 1.

- 1b. The given cultivation stage is invalid.
  - 1b1. SupporterBook shows an error message indicating the valid cultivation stages.

    Use case resumes from step 1.

- 1c. The supporter is already at the requested cultivation stage.
  - 1c1. SupporterBook informs the user that the supporter is already at that cultivation stage and makes no changes.

    Use case ends.

### Non-Functional Requirements

1.  Should work on any _mainstream OS_ as long as it has Java `25` or above installed.
2.  Should work without requiring an installer.
3.  Should be distributable as a single JAR file of no more than 100 MB.
4.  Should be usable by a single user on one computer, without supporting concurrent or shared access to the same data.
5.  Should store all supporter data locally in a human-readable file that can be inspected and edited while the application is not running.
6.  Should be usable for its normal functionality without depending on a remote server or internet connection.
7.  Should clearly inform the user if supporter data cannot be loaded or saved, rather than failing silently.
8.  With up to 1000 supporters, each with up to 50 logged interactions, every command should complete within 1 second on a typical laptop.
9.  A user with above average typing speed for regular English text (i.e. not code, not system admin commands) should be able to accomplish most common supporter-management tasks faster using commands than using the mouse.
10. The GUI should work well at standard screen resolutions 1920x1080 or higher at 100% or 125% screen scaling, and should remain usable at resolutions 1280x720 or higher and 150% scaling.
11. Should not send any supporter data over the network, so that supporters' personal details stay on the user's computer.
12. Should not require the user to create an account or log in.

### Glossary

- **CRM (Customer Relationship Management system)**: Software that an organisation uses to keep track of its relationships with customers or supporters. Small charities often cannot afford one.
- **CSR (Corporate Social Responsibility)**: A company's programme for supporting causes such as charities, e.g. through donations, partnerships or employee volunteering. A _CSR contact_ is the person at a company who manages it.
- **Cultivation stage**: How far a supporter's relationship with the charity has progressed. Every supporter is at exactly one of these six stages:
  - **Prospect**: Identified as a possible supporter, not yet approached. This is the stage of a newly added supporter.
  - **Contacted**: First approach made, with no real conversation yet.
  - **Cultivating**: In an active relationship or discussion, not yet giving.
  - **Giving**: Currently donating (an individual) or partnering (a company).
  - **Lapsed**: Gave before, but is no longer giving.
  - **Declined**: Said no.
- **Epic**: A large user need that is broken down into several smaller user stories. The user stories above are grouped by epic.
- **Find filter**: The shorter list of supporters shown after a `find` command. It stays in place until `list`, `add` or `edit` is run.
- **Index**: The number shown beside a supporter in the currently displayed list, used by commands to refer to that supporter. The same supporter can have a different index after a `find`.
- **Interaction**: A dated record of one conversation or other contact with a supporter, with a short note of what happened. A supporter's interactions together form their _interaction history_.
- **Introduction chain**: A sequence of introductions linking supporters, e.g. a board member introduces a CSR contact, who then introduces a colleague.
- **IPC (Institution of a Public Character)**: A Singapore charity that is approved to issue tax-deductible receipts to its donors.
- **Mainstream OS**: Windows, Linux, Unix, or macOS
- **Supporter**: Anyone whose relationship with the charity SupporterBook keeps track of, e.g. an individual donor, a prospective donor, a contact at a partner company, a board member, or a volunteer who makes introductions.
- **Tier 1 charity**: Under the Code of Governance for Charities and IPCs (2023), a small or medium charity that is not an IPC, with gross annual receipts or total expenditure from $50,000 to under $10 million. Tier 2 covers all IPCs and large non-IPC charities.

---

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more _exploratory_ testing.
</box>

### Launch and shutdown

1. Initial launch
   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample supporters, some of whom already have logged interactions. The window size may not be optimal.

1. Saving window preferences
   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
      Expected: The most recent window size and location are retained.

1. _{ more test cases … }_

### Finding and listing supporters

1. Prerequisites: The list contains `Tan Wei Ming`, `Tan Ah Kow` and `Weiling Koh`, added in that order.

1. Test case: `find wei`<br>
   Expected: `Tan Wei Ming` and `Weiling Koh` are shown in that order, numbered 1 and 2. The message is `2 supporter(s) listed!`.

1. Test case: `find tan wei`<br>
   Expected: All three supporters are shown in their original order, demonstrating OR matching and replacement of the previous filter.

1. Test cases: `find KOH`, then `find weiming`<br>
   Expected: The first command shows `Weiling Koh`. The second shows an empty list because a keyword cannot cross a space in a name.

1. Test case: `list 123` after a `find`<br>
   Expected: Every supporter is shown in the order added. The message is `Listed all supporters.` and stored data is unchanged.

1. Test case: `find` without keywords<br>
   Expected: An invalid-command-format message is shown and the displayed list is unchanged.

### Deleting a supporter

1. Deleting a supporter without interactions
   1. Prerequisites: Use isolated test data. Run `list` with multiple supporters, where the 1st supporter has no logged interactions.

   1. Test case: `delete 1`<br>
      Expected: The 1st supporter disappears. The result shows their details followed by `0 logged interaction(s) were deleted with them.` on a second line. Other supporters keep their original order.

1. Deleting a supporter with interactions after a find
   1. Prerequisites: Add two test supporters named `DeleteTestOne` and `DeleteTestTwo`, each with a phone number. Run `find DeleteTest`. Log two interactions against index 1, and one note mentioning `DeleteTestOne` against index 2.

   1. Test case: Run `view 1`, then `delete 1`.<br>
      Expected: The message names `DeleteTestOne` and reports `2 logged interaction(s) were deleted with them.` The find results now contain only `DeleteTestTwo`, numbered 1.

   1. Test case: Run `view 1`, then close and relaunch the app.<br>
      Expected: The view result reports one interaction for `DeleteTestTwo`. After relaunch, `DeleteTestOne` is absent, and `DeleteTestTwo` and their history are retained. Their saved note still mentions `DeleteTestOne` unchanged.

1. Invalid deletion commands
   1. Prerequisites: Run a `find` showing exactly one supporter.

   1. Test case: `delete 0`<br>
      Expected: An invalid-command-format message is shown. No supporter or history is deleted, and the list is unchanged.

   1. Test case: `delete 2`<br>
      Expected: `The supporter index provided is invalid.` is shown, even if the full supporter list has more supporters. The displayed list and data are unchanged.

   1. Other incorrect commands to try: `delete`, `delete abc`, `delete -1` and `delete 1 2`.<br>
      Expected: An invalid-command-format message, with no data or list changes.

### Setting a supporter's stage

1. Changing the stage while all supporters are being shown
   1. Prerequisites: List all supporters using the `list` command, with multiple supporters in the list. The 1st supporter is not at stage `Giving`.

   1. Test case: `stage 1 giving`<br>
      Expected: The 1st supporter's badge changes to `Giving`. The status message shows the old and new stages, and the detail panel switches to that supporter with stage `Giving`.

   1. Test case: `stage 1 giving` again<br>
      Expected: The record is unchanged. The status message says the supporter is already at stage `Giving`, and is not shown as an error. The detail panel shows that supporter even if another supporter was viewed before the command.

   1. Test case: `stage 1 GIVING`, then `stage 1 lapsed`<br>
      Expected: Upper case is accepted; the 1st supporter moves to `Lapsed`.

   1. Test case: `stage 1 gave`<br>
      Expected: No stage is changed. The status message lists the six valid stages.

   1. Other incorrect stage commands to try: `stage`, `stage 1`, `stage 0 giving`, `stage 1 giving lapsed`, `stage x giving` (where x is larger than the list size)<br>
      Expected: No stage is changed. The status message shows error details.

1. Changing the stage after a `find`
   1. Prerequisites: Run a `find` command that shows at least one supporter.

   1. Test case: `stage 1 cultivating`<br>
      Expected: The 1st supporter in the results moves to `Cultivating`, and the list still shows only the `find` results.

1. Setting a stage when adding a supporter
   1. Test case: `add n/Stage Tester p/91234567 e/tester@example.com a/1 Test Street st/contacted`<br>
      Expected: The new supporter is added with the `Contacted` badge.

   1. Test case: the same command without `st/contacted`, and with a different name<br>
      Expected: The new supporter is added with the `Prospect` badge.

### Logging an interaction

1. Logging while all supporters are being shown
   1. Prerequisites: List all supporters using the `list` command, with at least one supporter in the list.

   1. Test case: `log 1 nt/Called to discuss the year-end appeal`<br>
      Expected: An interaction dated today is logged against the 1st supporter. The status message shows the interaction number, supporter, date and note. The displayed list does not change. The detail panel switches to that supporter and shows the new entry in the sorted history.

   1. Test case: Repeat `log 1 nt/Called to discuss the year-end appeal`<br>
      Expected: The duplicate is accepted as a separate interaction. Its interaction number is one higher than the previous interaction.

1. Logging after a `find`
   1. Prerequisites: Run a `find` command that shows at least one supporter.

   1. Test case: `log 1 nt/Followed up after the meeting`<br>
      Expected: The interaction is logged against the 1st supporter in the results, and the list still shows only the `find` results.

1. Invalid interaction logging commands
   1. Test case: `log 1 nt/`<br>
      Expected: No interaction is logged. The status message says that an interaction note must not be blank and must be at most 500 characters long.

   1. Test case: `log 1 nt/Called nt/Emailed`<br>
      Expected: No interaction is logged. The status message reports the repeated `nt/` prefix.

   1. Other incorrect commands to try: `log`, `log 1`, `log 1 Called`, `log 0 nt/Called`, and `log 999 nt/Called` (where `999` is larger than the displayed list size)<br>
      Expected: No interaction is logged. The status message shows error details.

### Editing an interaction

1. Editing a date and then using the refreshed number
   1. Prerequisites: Add a supporter with a unique name using `add n/Editlog Tester p/91234567`, then run `find Editlog Tester`. Ensure exactly one supporter is displayed with no interactions. Run `log 1 nt/Middle meeting dt/2024-02-29`, `log 1 nt/First meeting dt/2024-02-20`, then `log 1 nt/Recent call dt/2024-03-10`.

   1. Test case: `editlog 1 2 dt/2024-03-15`<br>
      Expected: `Middle meeting` moves to #3 above `Recent call` (#2) and `First meeting` (#1). The success message reports #3, and last contact becomes 15 March 2024. The note, supporter details and find result are preserved.

   1. Test case: `editlog 1 3 nt/Corrected meeting`<br>
      Expected: The same entry's note changes, with its date and number unchanged.

   1. Test case: `editlog 1 3 dt/2024-03-10`<br>
      Expected: `Recent call` appears as #3 above `Corrected meeting` (#2). Both have the same date, but `Recent call` was logged later. Last contact becomes 10 March 2024.

1. Editing one of two identical entries
   1. Prerequisites: Add another uniquely named supporter with `add n/Editlog Duplicate Tester p/91234567`, then run `find Duplicate`. Ensure only that supporter is displayed with no interactions. Run `log 1 nt/Meeting dt/2024-02-29`, `log 1 nt/Follow-up dt/2024-02-29`, then `log 1 nt/Meeting dt/2024-02-29`.

   1. Test case: `editlog 1 3 nt/Corrected meeting`<br>
      Expected: The panel shows `Corrected meeting` (#3), `Follow-up` (#2) and the unchanged `Meeting` (#1).

1. Invalid edit commands
   1. Test cases: `editlog`, `editlog 1 nt/Note`, `editlog 1 abc nt/Note` and `editlog 1 2 3 nt/Note`<br>
      Expected: Invalid-command-format feedback with editlog usage.

   1. Test case: `editlog 1 1`<br>
      Expected: A message requiring at least one field to edit.

   1. Test cases: `editlog 1 1 nt/`, `editlog 1 1 dt/2024-02-30`, `editlog 1 1 dt/1899-12-31`, and a date after today<br>
      Expected: The corresponding note or date validation error.

   1. Test cases: `editlog 1 1 nt/First nt/Second` and `editlog 1 1 dt/2024-02-29 dt/2024-03-01`<br>
      Expected: A repeated-prefix error.

   1. Test cases: `editlog 2 1 nt/Note` when only one supporter is displayed; `editlog 1 4 nt/Note` when that supporter has three entries<br>
      Expected: Distinct invalid-supporter-index and invalid-interaction-number errors. All invalid commands above leave data, filtering and selection unchanged.

### Deleting an interaction

1. Deleting by chronological number after a `find`
   1. Prerequisites: Add a supporter with a unique name using `add n/Unlog Tester p/91234567`, then run `find Unlog Tester`. Ensure exactly one supporter is displayed with no interactions. Run these commands in order: `log 1 nt/Middle meeting dt/2024-02-29`, `log 1 nt/First meeting dt/2024-02-20`, `log 1 nt/Recent call dt/2024-03-10`.

   1. Test case: `unlog 1 2`<br>
      Expected: The success message identifies `Middle meeting` as deleted entry #2. The panel shows `Recent call` as #2 and `First meeting` as #1. The find result and supporter details remain unchanged.

   1. Continuing from the previous test case: `unlog 1 2`, then `unlog 1 1`<br>
      Expected: `Recent call` is removed first, then `First meeting`. The supporter remains selected with an empty history and `No interactions yet` as last contact.

1. Deleting one of two identical entries
   1. Prerequisites: With Unlog Tester still the only displayed supporter and their history empty, run `log 1 nt/Meeting dt/2024-02-29`, `log 1 nt/Follow-up dt/2024-02-29`, then `log 1 nt/Meeting dt/2024-02-29`.

   1. Test case: `unlog 1 3`<br>
      Expected: Only the later `Meeting` entry is removed. The panel shows `Follow-up` as #2 above the remaining `Meeting` as #1.

1. Invalid deletion commands
   1. Test cases: `unlog`, `unlog 1`, `unlog 1 2 3`, `unlog 0 1` and `unlog 1 abc`<br>
      Expected: An invalid-format message with unlog usage. Data and selection remain unchanged.

   1. Test cases: `unlog 2 1` when only one supporter is displayed; `unlog 1 3` when that supporter has only two entries<br>
      Expected: An invalid-supporter-index error for the first command and an invalid-interaction-number error for the second. Neither changes data or selection.

### Viewing a supporter's details and interaction history

1. Viewing a supporter with no interactions

   1. Prerequisites: Add a supporter with a unique name using `add n/View Tester p/91234567`, then run `find View Tester`. Ensure exactly one supporter is displayed.

   1. Test case: `view 1`<br>
      Expected: `Showing View Tester: no interactions logged yet`. The panel shows the name, stage and phone, with organisation, email, address and tags shown as `-`. The history area reads `No interactions yet. Log one with: log INDEX nt/NOTE.` The list and filter are unchanged.

1. Viewing a supporter with interactions

   1. Prerequisites: Keep View Tester as the only displayed supporter. Run `log 1 nt/Called about volunteering` and `log 1 nt/Met to discuss the appeal`.

   1. Test case: `view 1`<br>
      Expected: `Showing View Tester: 2 interaction(s) logged`. The panel shows `#2` before `#1` because both entries have today's date. Each row shows its number, date and complete note, with no list or filter change.

1. Viewing by the current displayed index

   1. Prerequisites: Run `list` with at least two supporters. Note the name at index 2, then use `find` to show only that supporter.

   1. Test case: `view 1`<br>
      Expected: The message and detail panel show the supporter who was previously at index 2. Only that supporter remains in the displayed list.

1. Viewing a sample supporter's history

   1. Prerequisites: Launch the app with no `data/supporterbook.json`, so that sample supporters are loaded, then run `list`.

   1. Test case: `view 1`<br>
      Expected: `Showing Alex Yeoh: 2 interaction(s) logged`. The history shows `#2  12 Sep 2026` before `#1  18 Jul 2026`.

1. Invalid view commands

   1. Test cases: `view`, `view abc`, `view 0`, `view -1`, `view 1.5`, `view 2147483648` and `view 1 2`<br>
      Expected: `Invalid command format!` followed by the `view` usage message.

   1. Test case: With exactly one supporter displayed, run `view 2`.<br>
      Expected: `The supporter index provided is invalid.`

   1. Test case: Run a `find` that returns no supporters, then `view 1`.<br>
      Expected: `The supporter index provided is invalid.`

   Invalid view commands must also leave the detail panel unchanged.

1. Startup and selection retention

   1. Test case: Restart the app.<br>
      Expected: The panel reads `Type view INDEX to see a supporter's details and history.`, even if a supporter was selected before exiting.

   1. Prerequisites: Run `list` with at least two supporters, then `view 1`. Note the displayed supporter's name.

   1. Test case: Run `find` with a name that excludes the viewed supporter, then `list`, then `add n/Panel Retention Tester p/91234567` using a unique name.<br>
      Expected: The list changes after each command, but the panel keeps showing the original supporter and retains its history scroll position.

1. Switching and clearing selection

   1. Prerequisites: Run `list` with at least two supporters and `view 1`.

   1. Test case: `log 2 nt/Panel switch test`<br>
      Expected: The panel switches to supporter 2 and shows their updated history.

   1. Test case: Run `view 1`, then `stage 2 giving`. Run `view 1` again, then repeat `stage 2 giving`.<br>
      Expected: Both stage commands switch the panel to supporter 2 with stage `Giving`, including the second command that reports no record change.

   1. Test case: Delete supporter 1 while supporter 2 is displayed in the panel.<br>
      Expected: The panel still shows the same supporter, whose displayed list index is now 1.

   1. Test case: `delete 1`<br>
      Expected: The displayed supporter is deleted, the panel removes their details and history, and the startup prompt returns.

   1. Test case: View a remaining supporter, then run `clear`.<br>
      Expected: The list is empty and the panel shows the startup prompt.

1. Long notes, scrolling and resizing

   1. Prerequisites: Use a test supporter with enough logged interactions to exceed the history area's height. Include a note close to the 500-character limit.

   1. Test case: View that supporter, scroll to the oldest entry, and resize the window or move the divider.<br>
      Expected: Notes wrap within the history area without truncation or horizontal scrolling. All entries remain reachable by vertical scrolling, and their numbers are unchanged. The list and panel stay side by side.

   1. Test case: While scrolled down, run `list` or `find`.<br>
      Expected: The same supporter and scroll position remain visible. A subsequent `log` for that supporter refreshes the history and returns it to the top.

### Saving data

1. Dealing with missing/corrupted data files

   1. Prerequisites: Use an isolated test folder with the JAR file. Keep a backup of any real supporter data and close the app before editing the test data file.

   1. Test case: Launch the app with no `data/supporterbook.json` in the test folder.<br>
      Expected: Sample supporters are shown, with no load-failure warning. No supporter data file is created until a command executes successfully.

   1. Test case: Run `list`, close the app, then relaunch it.<br>
      Expected: The saved supporters load, with no load-failure warning.

   1. Test case: Close the app, replace the test data file's contents with `not json format!`, then relaunch.<br>
      Expected: The list is empty and the result display warns that the file could not be loaded and that the next command will overwrite it. The invalid file's contents are unchanged at startup.

   1. Test case: After the warning, enter an unknown command.<br>
      Expected: `Unknown command.` replaces the warning, and the invalid file is unchanged.

   1. Test case: Preserve a copy of the invalid test file, then run `list`.<br>
      Expected: `Listed all supporters.` replaces the warning. The list remains empty and an empty valid data file replaces the invalid file.

1. Dealing with an invalid cultivation stage in the data file
   1. Prerequisites: Run the app once and close it, so that the data file has been created.

   1. Test case: In the data file, change one supporter's `"stage"` value to `"Gave"`, then launch the app.<br>
      Expected: The app starts with an empty list, as the data file is invalid.

   1. Test case: In the data file, delete one supporter's `"stage"` line, then launch the app.<br>
      Expected: Similar to previous.

   1. Test case: In the data file, change one supporter's `"stage"` value to `"giving"` (lower case), then launch the app.<br>
      Expected: The data loads, as stages are not case-sensitive.

1. _{ more test cases … }_

---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# SupporterBook Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

This project is based on the AddressBook-Level3 project created by the [SE-EDU initiative](https://se-education.org).

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

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
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Cultivation stage field

Every `Person` has exactly one cultivation stage (see the [Glossary](#glossary) for what each stage means). The stage is represented by the `Stage` enumeration in the `Model` component, with the six values `PROSPECT`, `CONTACTED`, `CULTIVATING`, `GIVING`, `LAPSED` and `DECLINED`.

* `Stage#isValidStage(String)` accepts any of the six stage names, ignoring case and surrounding whitespace, and `Stage#fromString(String)` converts such a string into a `Stage`.
* `Stage#toString()` returns the stage with a capital first letter (e.g. `Giving`). This is the form that is saved to the data file and shown to the user.
* `Stage.DEFAULT_STAGE` is `PROSPECT`. The `Person` constructor without a stage parameter uses it, so code that creates a `Person` without knowing about stages still works.
* `EditCommand` copies the existing stage into the edited `Person`, so editing other details never resets a supporter's stage.

In the `Storage` component, `JsonAdaptedPerson` saves the stage as a `"stage"` string. When the data file is loaded, a missing or unrecognised stage makes the whole file invalid, in the same way as any other invalid field, so the app starts with an empty list rather than silently guessing a stage.

In tests, `PersonBuilder` gives every `Person` the default stage unless `PersonBuilder#withStage(String)` is called.

#### Design considerations:

**Aspect: How to represent a stage**

* **Alternative 1 (current choice):** An enumeration of the six stages.
  * Pros: Only valid stages can exist, and later features that filter or count by stage can compare values directly.
  * Cons: Adding a new stage needs a code change.

* **Alternative 2:** A free-text field, validated like `Address`.
  * Pros: Users could invent their own stages.
  * Cons: Typos such as `givng` would create stages that no filter or count would match.

**Aspect: What to do when a saved stage is missing**

* **Alternative 1 (current choice):** Treat the data file as invalid.
  * Pros: Consistent with every other required field; a corrupted file is noticed instead of silently changing supporters' stages.
  * Cons: A data file saved before stages existed cannot be loaded.

* **Alternative 2:** Load the supporter at the default stage.
  * Pros: Old data files still load.
  * Cons: A supporter who was `Giving` could silently become a `Prospect` if the field was deleted by mistake.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` -- Saves the current address book state in its history.
* `VersionedAddressBook#undo()` -- Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` -- Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.
</box>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />


<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.
</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</box>

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.
</box>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …` command. This is the behavior that most modern desktop applications follow.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* Donor and corporate-partnership executives at small (Tier 1) charities, who single-handedly manage every supporter relationship without access to a CRM.

**Value proposition**: SupporterBook helps the sole fundraising and outreach staff at a small charity stay on top of every donor and corporate relationship, keeping each supporter’s history and the introductions behind it in one place, so follow-ups happen on time and relationships outlast whoever holds the role. It also allows fast CLI-based retrieval.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (later version) - `*`

Stories are grouped by epic, with the highest-priority stories listed first within each epic.

#### A. Getting started

| Priority | As a …                                           | I want to …                                                                     | So that …                                                                          |
|----------|--------------------------------------------------|---------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| `*`      | potential user exploring the app                 | see the app pre-populated with sample supporter records                         | I can tell what it will look like once my own data is in it                        |
| `*`      | potential user exploring the app                 | see a list of all available commands with an example of each                    | I can learn what the app does without reading a manual                             |
| `*`      | user ready to start using the app for real       | clear all sample data in one command                                            | I can begin from a clean list of my own supporters                                 |
| `*`      | new user who already keeps a spreadsheet         | import my existing supporter list from a CSV file                               | I do not have to retype 250 contacts before the app is of any use                  |
| `*`      | new user                                         | see where the app stores my data file                                           | I know what to back up and what to hand over                                       |

#### B. Contact records

| Priority | As a …                                           | I want to …                                                                     | So that …                                                                          |
|----------|--------------------------------------------------|---------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| `* * *`  | fundraiser                                       | add a supporter with their name and contact details                             | every supporter sits in one place instead of in my inbox                           |
| `* * *`  | fundraiser                                       | see a list of all my supporters                                                 | I can see everyone I am responsible for                                            |
| `* * *`  | fundraiser                                       | delete a supporter                                                              | my list does not fill up with people I will never contact again                    |
| `* * *`  | fundraiser                                       | edit a supporter's details                                                      | their phone and email stay current when they change jobs                           |
| `* * *`  | fundraiser                                       | record the organisation a contact belongs to                                    | I know which company a CSR contact speaks for                                      |
| `* *`    | fundraiser                                       | view one supporter's whole record on a single screen                            | I can prepare for a meeting in the minute before it starts                         |
| `* *`    | fundraiser                                       | add free-form notes to a supporter                                              | I can keep the things that do not fit any field                                    |
| `* *`    | fundraiser recording Singaporean names           | save names containing hyphens, apostrophes, s/o and d/o                         | I can record my supporters' names as they actually are                             |
| `* *`    | fundraiser with overseas corporate contacts      | save phone numbers that include a country code                                  | I can store the numbers I actually dial                                            |
| `* *`    | fundraiser                                       | mark whether a contact is an individual supporter or a corporate contact        | I can tell the two kinds of relationship apart                                     |
| `*`      | fundraiser                                       | record a contact's job title                                                    | I know whether I am talking to someone who can approve a partnership               |
| `*`      | fundraiser                                       | record how a supporter prefers to be addressed                                  | I do not open a letter to a major donor with the wrong name                        |

#### C. Finding and retrieving

| Priority | As a …                                           | I want to …                                                                     | So that …                                                                          |
|----------|--------------------------------------------------|---------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| `* * *`  | fundraiser                                       | find a supporter by part of their name                                          | I can pull up a record without remembering the exact spelling                      |
| `*`      | fundraiser                                       | search on fields other than the name                                            | I can find the person at a particular bank when their name escapes me              |
| `*`      | forgetful user                                   | be warned when I add someone whose name closely matches an existing contact     | I do not create a second record for a person I already have                        |
| `*`      | fundraiser                                       | filter my supporters by cultivation stage                                       | I can work through everyone at one stage of the relationship                       |
| `*`      | fundraiser                                       | combine more than one filter in a single search                                 | I can ask a precise question such as corporate contacts untouched since June       |
| `*`      | fundraiser                                       | sort my list by the date I last spoke to each person                            | whoever has gone quiet rises to the top                                            |
| `*`      | fundraiser                                       | see how many supporters match a search                                          | I know the size of the job before I start working through it                       |

#### D. Interaction history

| Priority | As a …                                           | I want to …                                                                     | So that …                                                                          |
|----------|--------------------------------------------------|---------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| `* * *`  | fundraiser                                       | log an interaction against a supporter with a date and a short note             | what was said stops living only in my memory                                       |
| `* * *`  | fundraiser                                       | see all logged interactions for a supporter in date order                       | I can see how the relationship has developed                                       |
| `* *`    | fundraiser                                       | correct or remove a logged interaction                                          | a mistyped date does not stay wrong forever                                        |
| `* *`    | fundraiser                                       | see the date of the most recent interaction beside each supporter in the list   | I can see at a glance who has gone quiet                                           |
| `*`      | fundraiser                                       | log an interaction dated in the past                                            | I can write up a meeting the week after it happened                                |
| `*`      | fundraiser                                       | record what kind of interaction it was, such as a call, a meeting or an email   | I can tell a coffee meeting apart from a mass mailing                              |
| `*`      | fundraiser                                       | record the next step I intend to take with a supporter                          | I know what I promised to do next                                                  |
| `*`      | fundraiser                                       | see which intended next steps are overdue                                       | nothing I promised slips silently                                                  |

#### E. Introductions and relationships

| Priority | As a …                                           | I want to …                                                                     | So that …                                                                          |
|----------|--------------------------------------------------|---------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| `*`      | fundraiser                                       | record who introduced me to a supporter                                         | I know whose goodwill created the relationship                                     |
| `*`      | fundraiser                                       | see everyone a given person has introduced me to                                | I can tell which board member opens the most doors                                 |
| `*`      | fundraiser                                       | trace an introduction chain back to where it started                            | I can see how a major partnership actually came about                              |
| `*`      | fundraiser                                       | see which introductions I have not yet reported back on                         | the person who made them keeps making them                                         |
| `*`      | fundraiser                                       | link contacts who share an affiliation such as a company or a board             | I can see everyone I know at one organisation                                      |

#### F. Cultivation and prioritisation

| Priority | As a …                                           | I want to …                                                                     | So that …                                                                          |
|----------|--------------------------------------------------|---------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| `* * *`  | fundraiser                                       | set a cultivation stage on a supporter                                          | I know whether they have been approached, are giving, have lapsed or have declined |
| `* * *`  | fundraiser                                       | change a supporter's stage in a single command                                  | updating where things stand after a meeting takes seconds                          |
| `* *`    | fundraiser                                       | tag supporters with labels of my own                                            | I can group them in ways I did not anticipate when I started                       |
| `*`      | fundraiser                                       | see how many supporters sit at each stage                                       | I can tell early in the year whether I have enough prospects                       |
| `*`      | fundraiser                                       | record that a supporter declined, together with a reason                        | I do not approach them again the same way                                          |
| `*`      | fundraiser                                       | pin a small number of key relationships                                         | the handful my year depends on stay in front of me                                 |
| `*`      | fundraiser                                       | see supporters marked as giving who have had no interaction this financial year | lapsed givers surface before the year closes                                       |

#### G. Data safety and trust

| Priority | As a …                                           | I want to …                                                                     | So that …                                                                          |
|----------|--------------------------------------------------|---------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| `* * *`  | fundraiser                                       | have every change saved automatically                                           | I never lose work by forgetting to save                                            |
| `* *`    | careless user                                    | undo my last command                                                            | a mistaken delete does not cost me a record I cannot rebuild                       |
| `*`      | cautious user                                    | be asked to confirm before a command that wipes all my data                     | one stray keystroke cannot empty the list                                          |
| `*`      | fundraiser handling other people's personal data | keep everything in a file on my own machine with nothing sent anywhere          | supporters' details never leave the charity's laptop                               |
| `*`      | fundraiser                                       | open and repair my data file in a text editor                                   | a corrupted file does not mean starting again from nothing                         |
| `*`      | fundraiser                                       | export my supporters to a spreadsheet                                           | I can hand a list to my executive director without giving them the app             |

#### H. Speed for expert users

| Priority | As a …                                           | I want to …                                                                     | So that …                                                                          |
|----------|--------------------------------------------------|---------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| `*`      | expert user                                      | bring back previous commands with the up arrow                                  | I can repeat a near-identical command without retyping it                          |
| `*`      | expert user                                      | use short aliases for the commands I run most often                             | logging a call costs me a few keystrokes                                           |
| `*`      | expert user                                      | see the parameters of a command as I type it                                    | I do not have to remember the exact format of a command I use rarely               |
| `*`      | expert user                                      | delete a range of contacts in one command                                       | clearing up after a campaign does not take twenty commands                         |
| `*`      | expert user                                      | log one interaction against several contacts at once                            | recording a meeting with three people from one company takes one command           |

#### I. Continuity and handover

| Priority | As a …                                           | I want to …                                                                     | So that …                                                                          |
|----------|--------------------------------------------------|---------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| `*`      | newly hired executive inheriting the role        | read the interaction notes my predecessor left                                  | I am not starting every relationship from zero                                     |
| `*`      | departing executive                              | hand over a single data file that holds everything                              | what I know about our supporters stays with the charity                            |
| `*`      | returning user after weeks on an event           | see which relationships have gone stale while I was busy                        | I can pick up where I left off                                                     |
| `*`      | long-time user                                   | archive supporters I no longer actively cultivate without deleting them         | my working list stays short while the history survives                             |
| `*`      | fundraiser reporting to the board                | produce a summary of my activity over a period                                  | I can show what I have been doing without rebuilding it from memory                |

### Use cases

(For all use cases below, the **System** is the `SupporterBook` and the **Actor** is the `user`, unless specified otherwise)

**Use case: UC01 - Add a supporter**

**MSS**

1.  User requests to add a supporter, giving their details.
2.  SupporterBook adds the supporter and shows the full list with the new supporter at the end.

    Use case ends.

**Extensions**

* 1a. User did not provide a name.

    * 1a1. SupporterBook shows an error message.

      Use case resumes from step 1.

* 1b. User provided neither a phone number nor an email.

    * 1b1. SupporterBook shows an error message stating that at least one is required.

      Use case resumes from step 1.

* 1c. User provided a detail that is invalid (e.g. a name with symbols, a phone number with spaces, an unknown stage).

    * 1c1. SupporterBook shows an error message for that detail.

      Use case resumes from step 1.

* 1d. A supporter with the same name already exists, ignoring capitalisation and extra spaces.

    * 1d1. SupporterBook shows an error message suggesting a way to tell the two supporters apart.

      Use case resumes from step 1.

* 1e. User gives more than one value for a detail that takes only one value (e.g. two names).

    * 1e1. SupporterBook shows an error message, indicating the detail with multiple values given.

      Use case resumes from step 1.

**Use case: UC02 - List all supporters**

**MSS**

1.  User requests to list all supporters.
2.  SupporterBook shows the list of every supporter, clearing any find filter.

    Use case ends.

**Extensions**

* 2a. No supporters have been added yet.

    * 2a1. SupporterBook shows an empty list.

      Use case ends.

**Use case: UC03 - Find supporters by part of a name**

**MSS**

1.  User requests to find supporters whose name contains certain keywords, giving the keyword(s).
2.  SupporterBook shows the list of every supporter whose name contains at least one of the keywords given, replacing any previous find filter.

    Use case ends.

**Extensions**

* 1a. User did not provide any keywords.

    * 1a1. SupporterBook shows an error message with the correct command format.

      Use case resumes from step 1.

* 2a. No supporter's name contains any of the keywords given.

    * 2a1. SupporterBook shows an empty list.

      Use case ends.

**Use case: UC04 - Edit a supporter**

**Preconditions: A list of supporters is shown, with an index beside each supporter**

**MSS**

1.  User requests to edit an existing supporter's details, giving an index and the field(s) to edit.
2.  SupporterBook updates only the specified field(s) of the supporter, without altering the supporter's interaction history.
3.  SupporterBook shows the list of every supporter, clearing any find filter. If the edited supporter's details were being shown, SupporterBook shows the updated details.

    Use case ends.

**Extensions**

* 1a. The given index is invalid.

    * 1a1. SupporterBook shows an error message.

      Use case resumes from step 1.

* 1b. User does not give a field.

    * 1b1. SupporterBook shows an error message, suggesting for the user to provide at least one field.

      Use case resumes from step 1.

* 1c. User gives multiple values for the same field (excluding the tags field).

    * 1c1. SupporterBook shows an error message, indicating the field with multiple values given.

      Use case resumes from step 1.

* 1d. User gives invalid values for certain fields.

    * 1d1. SupporterBook shows an error message, indicating the field with invalid value.

      Use case resumes from step 1.

* 1e. The new name is the same as another supporter's name, ignoring capitalisation and extra spaces.

    * 1e1. SupporterBook shows an error message, suggesting a way to tell the two supporters apart.

      Use case resumes from step 1.

**Use case: UC05 - Delete a supporter**

**Preconditions: A list of supporters is shown, with an index beside each supporter**

**MSS**

1.  User requests to delete a specific supporter in the list, giving an index.
2.  SupporterBook deletes the supporter, together with every interaction logged with them.
3.  SupporterBook confirms the deletion, stating how many logged interactions were deleted with the supporter.

    Use case ends.

**Extensions**

* 1a. The given index is invalid.

    * 1a1. SupporterBook shows an error message.

      Use case resumes from step 1.

* 2a. The deleted supporter's details were being shown.

    * 2a1. SupporterBook stops showing that supporter's details.

      Use case resumes from step 3.

**Use case: UC06 - Log an interaction with a supporter**

**Preconditions: A list of supporters is shown, with an index beside each supporter**

**MSS**

1.  User requests to log an interaction with a specific supporter in the list, giving an index and a short note.
2.  SupporterBook records the interaction, dated today, in the supporter's interaction history.
3.  SupporterBook confirms the logged interaction and shows the supporter's details and updated interaction history.

    Use case ends.

**Extensions**

* 1a. The given index is invalid.

    * 1a1. SupporterBook shows an error message.

      Use case resumes from step 1.

* 1b. User does not give a note.

    * 1b1. SupporterBook shows an error message.

      Use case resumes from step 1.

* 1c. The given note is blank or longer than 500 characters.

    * 1c1. SupporterBook shows an error message stating the rules for a note.

      Use case resumes from step 1.

* 1d. User gives more than one note.

    * 1d1. SupporterBook shows an error message, indicating that only one note can be given.

      Use case resumes from step 1.

**Use case: UC07 - View a supporter's details and interaction history**

**Preconditions: A list of supporters is shown, with an index beside each supporter**

**MSS**

1.  User requests to view a specific supporter in the list, giving an index.
2.  SupporterBook shows the supporter's details and interaction history, with the most recent interactions shown first.

    Use case ends.

**Extensions**

* 1a. The given index is invalid.

    * 1a1. SupporterBook shows an error message.

      Use case resumes from step 1.

* 2a. The supporter has no logged interactions.

    * 2a1. SupporterBook shows the supporter's details and indicates that there are no interactions logged yet.

      Use case ends.

**Use case: UC08 - Set a supporter's cultivation stage**

**Preconditions: A list of supporters is shown, with an index beside each supporter**

**MSS**

1.  User requests to set the cultivation stage of a specific supporter in the list.
2.  SupporterBook updates the supporter's cultivation stage.
3.  SupporterBook confirms the change and shows the updated supporter.

    Use case ends.

**Extensions**

* 1a. The given index is invalid.

    * 1a1. SupporterBook shows an error message.

      Use case resumes from step 1.

* 1b. The given cultivation stage is invalid.

    * 1b1. SupporterBook shows an error message indicating the valid cultivation stages.

      Use case resumes from step 1.

* 1c. The supporter is already at the requested cultivation stage.

    * 1c1. SupporterBook informs the user that the supporter is already at that cultivation stage and makes no changes.

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

* **CRM (Customer Relationship Management system)**: Software that an organisation uses to keep track of its relationships with customers or supporters. Small charities often cannot afford one.
* **CSR (Corporate Social Responsibility)**: A company's programme for supporting causes such as charities, e.g. through donations, partnerships or employee volunteering. A _CSR contact_ is the person at a company who manages it.
* **Cultivation stage**: How far a supporter's relationship with the charity has progressed. Every supporter is at exactly one of these six stages:
    * **Prospect**: Identified as a possible supporter, not yet approached. This is the stage of a newly added supporter.
    * **Contacted**: First approach made, with no real conversation yet.
    * **Cultivating**: In an active relationship or discussion, not yet giving.
    * **Giving**: Currently donating (an individual) or partnering (a company).
    * **Lapsed**: Gave before, but is no longer giving.
    * **Declined**: Said no.
* **Epic**: A large user need that is broken down into several smaller user stories. The user stories above are grouped by epic.
* **Find filter**: The shorter list of supporters shown after a `find` command. It stays in place until `list`, `add` or `edit` is run.
* **Index**: The number shown beside a supporter in the currently displayed list, used by commands to refer to that supporter. The same supporter can have a different index after a `find`.
* **Interaction**: A dated record of one conversation or other contact with a supporter, with a short note of what happened. A supporter's interactions together form their _interaction history_.
* **Introduction chain**: A sequence of introductions linking supporters, e.g. a board member introduces a CSR contact, who then introduces a colleague.
* **IPC (Institution of a Public Character)**: A Singapore charity that is approved to issue tax-deductible receipts to its donors.
* **Mainstream OS**: Windows, Linux, Unix, or macOS
* **Supporter**: Anyone whose relationship with the charity SupporterBook keeps track of, e.g. an individual donor, a prospective donor, a contact at a partner company, a board member, or a volunteer who makes introductions.
* **Tier 1 charity**: Under the Code of Governance for Charities and IPCs (2023), a small or medium charity that is not an IPC, with gross annual receipts or total expenditure from $50,000 to under $10 million. Tier 2 covers all IPCs and large non-IPC charities.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases … }_

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases … }_

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. Dealing with an invalid cultivation stage in the data file

   1. Prerequisites: Run the app once and close it, so that the data file has been created.

   1. Test case: In the data file, change one supporter's `"stage"` value to `"Gave"`, then launch the app.<br>
      Expected: The app starts with an empty list, as the data file is invalid.

   1. Test case: In the data file, delete one supporter's `"stage"` line, then launch the app.<br>
      Expected: Similar to previous.

   1. Test case: In the data file, change one supporter's `"stage"` value to `"giving"` (lower case), then launch the app.<br>
      Expected: The data loads, as stages are not case-sensitive.

1. _{ more test cases … }_

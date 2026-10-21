---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# SupporterBook User Guide

SupporterBook is a **desktop application for fundraising and outreach staff at small charities to manage donor and corporate supporter relationships, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). It keeps supporter details, cultivation stages and interaction histories in one place. If you type quickly, SupporterBook can help you find and update supporter records faster than traditional GUI applications.

<!-- * Table of Contents -->
<page-nav-print />

---

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `supporterbook.jar` file from [here](https://github.com/AY2627S1-CS2103T-T14-2/tp/releases).

1. Copy the file to the folder you want to use as the _home folder_ for SupporterBook. Your data is saved in a `data` folder inside it.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar supporterbook.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample supporters. A few already have logged interactions, which you can see with `view`.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:
   - `list` : Lists all supporters.

   - `add n/Tan Wei Ming p/91234567 e/weiming@dbs.com o/DBS Bank` : Adds a supporter named `Tan Wei Ming` from `DBS Bank`.

   - `log 1 nt/Called about the year-end appeal` : Logs an interaction with the 1st supporter shown in the current list.

   - `view 1` : Shows the 1st supporter's interaction history.

   - `stage 1 contacted` : Sets the 1st supporter's cultivation stage to `Contacted`.

   - `delete 3` : Deletes the 3rd supporter shown in the current list, together with their interactions.

   - `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

---

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

- Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

- Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

- Items followed by `...` can appear zero or more times.<br>
  For example, `[t/TAG]... ` may be omitted, or written as `t/friend` or `t/friend t/family`.

- Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE`, `p/PHONE n/NAME` is also acceptable.

- Extraneous parameters for commands that take no parameters, such as `help`, `list`, `stats`, `exit`, and `clear`, are ignored.<br>
  For example, `help 123` is interpreted as `help`.

- If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
  </box>

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`

### Adding a supporter: `add`

Adds a supporter to SupporterBook.

Format: `add n/NAME [p/PHONE] [e/EMAIL] [a/ADDRESS] [o/ORGANISATION] [st/STAGE] [t/TAG]... `

- At least one of `p/PHONE` and `e/EMAIL` must be given, so that there is a way to reach the supporter. Online donors often give only an email, and people met at events often give only a number.
- `a/ADDRESS` can be left out, e.g. when you do not know a corporate contact's postal address.
- `ORGANISATION` is the company or group the supporter belongs to, e.g. the company a CSR contact speaks for. Leave out `o/ORGANISATION` for individual donors.
- `ORGANISATION` can contain any characters (e.g. `Procter & Gamble`), must not be blank, and can be at most 100 characters long. Extra spaces are removed.
- `STAGE` is the supporter's cultivation stage, and must be one of `prospect`, `contacted`, `cultivating`, `giving`, `lapsed` or `declined`. Upper and lower case are both accepted.
- If `st/STAGE` is left out, the new supporter starts at stage `Prospect`.
- `PHONE` must contain only digits, and be between 3 and 15 digits long.
- Extra spaces in `NAME` are removed, e.g. `n/  Tan   Wei Ming` is saved as `Tan Wei Ming`.
- Two supporters cannot have the same name. Names are compared ignoring capital letters and extra spaces, so `tan wei ming` is the same supporter as `Tan Wei Ming`. If two different people share a name, add something to tell them apart, e.g. `n/Tan Wei Ming DBS`.
- After adding, the list shows all supporters (any `find` filter is cleared), with the new supporter at the bottom.

<box type="tip" seamless>

**Tip:** A supporter can have any number of tags, including zero, e.g. `t/boardIntro` for someone introduced by a board member.
</box>

Examples:

- `add n/Aisha Rahman e/aisha.r@gmail.com` adds an online donor who gave only an email.
- `add n/John Lim p/81112222 e/johnlim@example.com a/Bedok North Ave 1 st/lapsed` adds a past donor at stage `Lapsed`.
- `add n/Tan Wei Ming p/91234567 e/weiming@dbs.com a/12 Marina Boulevard o/DBS Bank t/boardIntro` adds a CSR contact at DBS Bank who was introduced by a board member.

### Listing all supporters: `list`

Shows all supporters in the order they were added, clearing any active `find` filter.

Format: `list`

The success message is `Listed all supporters.`, even when the list is empty.
Anything typed after the command word is ignored; `list 123` behaves as `list`.

### Editing a supporter: `edit`

Edits an existing supporter's details, keeping their stage and interaction history.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [o/ORGANISATION] [t/TAG]... `

- Edits the supporter at the specified `INDEX`. The index refers to the index number shown in the displayed supporter list, including after `find`. The index **must be a positive integer** 1, 2, 3, ...
- At least one of the optional fields must be provided.
- Existing values will be updated to the input values. Each value follows the same rules as in `add`.
- When editing tags, all of the supporter's existing tags are removed; adding tags is not cumulative.
- To remove all of a supporter's tags, enter `t/` without a tag after it.
- A supporter's stage cannot be changed with `edit`, and stays the same when other details are edited. Use the `stage` command instead.
- A supporter's logged interactions are kept when their details are edited.
- A supporter's introducer is kept when their details are edited. If you rename a supporter who introduced others, those supporters show the new name as their introducer.
- A supporter's name cannot be changed to the name of another supporter, using the same comparison as `add`. Changing only the capitalisation of their own name is allowed.
- After editing, the list shows all supporters (any `find` filter is cleared).

Examples:

- `edit 1 p/91234567 e/weiming@dbs.com` Edits the phone number and email address of the 1st supporter to be `91234567` and `weiming@dbs.com` respectively.
- `edit 2 n/Tan Wei Ming DBS t/` Edits the name of the 2nd supporter to be `Tan Wei Ming DBS` and clears all existing tags.
- `edit 3 o/OCBC Bank` Changes the organisation of the 3rd supporter to `OCBC Bank`, e.g. after they change jobs.

### Setting a supporter's stage: `stage`

Changes how far the relationship with a supporter has progressed.

Format: `stage INDEX STAGE`

- Sets the stage of the supporter at the specified `INDEX`. The index refers to the index number shown in the displayed supporter list. The index **must be a positive integer** 1, 2, 3, ...
- `STAGE` must be one of the six stages below. Upper and lower case are both accepted. No prefix is needed before it.
- Any stage can be changed to any other stage, e.g. a lapsed donor can be moved back to `cultivating`.
- If the supporter is already at that stage, their record is unchanged and a message says so.
- On success, the detail panel switches to this supporter and shows their current stage, including when they are already at the requested stage.
- The displayed list stays the same, so a `find` result is kept.

| Stage         | Meaning                                                                            |
| ------------- | ---------------------------------------------------------------------------------- |
| `Prospect`    | Identified as a possible supporter, not yet approached. New supporters start here. |
| `Contacted`   | First approach made, with no real conversation yet.                                |
| `Cultivating` | In an active relationship or discussion, not yet giving.                           |
| `Giving`      | Currently donating (an individual) or partnering (a company).                      |
| `Lapsed`      | Gave before, but is no longer giving.                                              |
| `Declined`    | Said no.                                                                           |

Each supporter's stage is shown as a coloured badge next to their name in the list.

Examples:

- `stage 2 cultivating` moves the 2nd supporter in the displayed list to stage `Cultivating`.
- `find Tan` followed by `stage 1 GIVING` moves the 1st supporter in the results of the `find` command to stage `Giving`.

### Counting supporters by stage: `stats`

Shows how many supporters are at each cultivation stage, e.g. to check whether there are enough prospects early in the year.

Format: `stats`

- Every supporter is counted, including those hidden by a `find` filter.
- All six stages are listed in order, including stages with no supporters.
- The displayed list and the detail panel do not change.
- Anything typed after the command word is ignored; `stats 123` behaves as `stats`.

For example, with four supporters:

```text
Supporters by stage (4 in total):
Prospect: 1
Contacted: 0
Cultivating: 0
Giving: 2
Lapsed: 1
Declined: 0
```

### Finding supporters by part of a name: `find`

Shows supporters whose names contain at least one of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]...`

- The search is case-insensitive; `KOH` matches `Weiling Koh`.
- A keyword can match any part of a word; `wei` matches both `Tan Wei Ming` and `Weiling Koh`.
- Supporters matching any keyword are returned (an `OR` search). Keyword order and repeated keywords do not affect the results.
- Only names are searched. A keyword such as `@` is accepted and simply matches nobody.
- Keywords are separated by whitespace and cannot span a space in a name: `weiming` does not match `Wei Ming`.
- Results stay in their original list order and are numbered from 1. Other commands use these displayed indices.
- A new `find` replaces the previous filter. The filter stays active until `list`, `add` or `edit` is run.
- At least one keyword is required. `find` with no keywords shows an invalid-command-format message.

For a list containing `Tan Wei Ming`, `Tan Ah Kow` and `Weiling Koh`:

- `find wei` returns `Tan Wei Ming` and `Weiling Koh`.
- `find tan wei` returns all three supporters.
- `find KOH` returns `Weiling Koh`.
- `find tan tan` returns the same results as `find tan`.

The success message reports the number of matches, for example `3 supporter(s) listed!`.
If nothing matches, the list is empty and the message is `0 supporter(s) listed!`.
Run `list` to show all supporters again.

### Deleting a supporter: `delete`

Permanently removes the specified supporter together with every interaction logged with them.

Format: `delete INDEX`

- Deletes the supporter at the specified `INDEX`, including their entire interaction history.
- The index refers to the index number shown in the displayed supporter list, including after `find`.
- The index **must be a positive integer** 1, 2, 3, ...
- The success message reports the number of interactions removed, including `0` when there were none. Identical entries are counted separately.
- The current find filter is retained, and supporters below the deleted entry move up one number.
- If the supporter is shown in the detail panel, their details and history are cleared and the startup prompt returns. Deleting another supporter preserves the panel's displayed supporter.
- Other supporters' details and interaction histories are unchanged, including notes that mention the deleted supporter.
- If the deleted supporter introduced others, they are removed as those supporters' introducer, and the success message says how many supporters this affected.

Examples:

- `list` followed by `delete 2` deletes the 2nd supporter in the full list.
- `find Tan` followed by `delete 1` deletes the 1st supporter in the search results and keeps the filter.

For a supporter with two interactions, the success message has two lines:

```text
Deleted supporter: Tan Wei Ming; Stage: Prospect; Phone: 91234567
2 logged interaction(s) were deleted with them.
```

Deletion has no confirmation prompt or undo in this version. An invalid command or an index beyond the displayed list leaves supporter data, the list and the viewed selection unchanged.

### Logging an interaction: `log`

Records an interaction with a supporter, dated today.

Format: `log INDEX nt/NOTE`

- Logs the interaction against the supporter at the specified `INDEX`. The index refers to the index number shown in the displayed supporter list and must be a positive integer.
- `NOTE` records what happened. It can contain any characters, must not be blank, and can be at most 500 characters long. Leading and trailing spaces are removed.
- The interaction is automatically dated using the computer's current date.
- Interactions are numbered in the order they are logged, starting from `#1` for each supporter.
- Identical notes logged on the same day are accepted as separate interactions.
- On success, the result display shows the interaction number, supporter, date and note.
- The detail panel switches to this supporter and shows their updated history, with the newest interaction first.
- Logging an interaction does not change the supporter's cultivation stage or the currently displayed list. A `find` result therefore remains in place.

Examples:

- `log 2 nt/Coffee at Raffles Place, keen on a Q4 volunteering day`
- `find Tan` followed by `log 1 nt/Left voicemail about the year-end appeal` logs the interaction against the 1st supporter in the search results.

<box type="warning" seamless>

If a note contains a space followed by `nt/`, that text is treated as a second note prefix and the command is rejected. Rephrase the note to avoid `nt/`.
</box>

### Viewing a supporter's details and interaction history: `view`

Shows a supporter's full record and every logged interaction in the detail panel beside the supporter list. The result display reports their name and interaction count.

Format: `view INDEX`

* `INDEX` is required and must be a positive integer from the currently displayed supporter list. After a `find`, use the index shown in the search results.
* Supply exactly one index, with no additional arguments.
* Viewing a supporter preserves the displayed list, its filter and all supporter data.
* The panel shows the name, stage, organisation, phone, email, address, tags, introducer and the supporters they introduced. Unset fields, including an empty tag list, appear as `-`. Tags are shown alphabetically.
* Interactions appear newest first, one entry per row, for example `#4  16 Sep 2026  Sent partnership deck by email`. Entries on the same date appear in descending entry-number order. Entry numbers are based on the order in which interactions were logged.
* The panel keeps showing the supporter while you run `list`, `find` or `add`, even if that supporter disappears from the displayed list. These commands also preserve the current history scroll position.
* A successful `view`, `log` or `stage` switches the panel to its target. Changes to the displayed supporter's record are reflected.
* Invalid commands leave the displayed supporter unchanged.
* Deleting the displayed supporter or running `clear` clears the details and history. Deleting another supporter keeps the panel unchanged.
* Selection is not remembered after restarting the app.

Examples:

* `view 2` selects the 2nd supporter in the displayed list.
* `find Priya` followed by `view 1` selects the 1st supporter in the search results.

### Recording who introduced a supporter: `intro`

Records which supporter introduced another, e.g. the board member who introduced you to a company's CSR lead. This lets you see whose goodwill created each relationship, and everyone a given person has introduced you to.

Format: `intro INDEX by/[INTRODUCER_INDEX]`

- `INDEX` is the supporter who was introduced, and `INTRODUCER_INDEX` is the supporter who introduced them. Both refer to the index numbers shown in the displayed supporter list and **must be positive integers** 1, 2, 3, ...
- Both supporters must be in the displayed list. If one of them is hidden by a `find` filter, use `find` with both names, e.g. `find tan lim`, or run `list` first.
- Running `intro` again for the same supporter replaces their introducer. Each supporter has at most one introducer.
- Leave out `INTRODUCER_INDEX` to remove the record, e.g. `intro 3 by/`.
- A supporter cannot be their own introducer.
- If nothing would change (the same introducer again, or removing a record that does not exist), a message says so and the record is unchanged.
- On success, the detail panel switches to the introduced supporter. The displayed list stays the same, so a `find` result is kept.
- The detail panel shows each supporter's `Introducer`, and under `Introduced`, everyone they introduced.
- Renaming an introducer with `edit` keeps their introductions. Deleting an introducer removes them as the introducer of everyone they introduced.

Examples:

- `intro 3 by/1` records that the 1st supporter in the displayed list introduced the 3rd.
- `find tan lim` followed by `intro 2 by/1` records an introduction between the two supporters shown.
- `intro 3 by/` removes the 3rd supporter's introducer.

### Clearing all entries: `clear`

Deletes all supporters, together with their logged interactions.

Format: `clear`

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

SupporterBook automatically saves data after every command that executes successfully. You do not need to save manually.

### Loading data at startup

SupporterBook loads `data/supporterbook.json` from the folder the app is launched from. The data file path is shown in the status bar.

* If the file is missing, the app starts with sample supporters. The file is created after the first command that executes successfully.
* If the file is valid, the app loads its supporters and lists them all.
* If the file cannot be loaded, the app starts with an empty list and shows a warning in the result display:

> Could not load data/supporterbook.json because it is not in the expected format. Starting with an empty list. Your next command will overwrite the file, so copy it somewhere safe now if you want to repair it.

The message includes the path of the file that could not be loaded. Invalid JSON, missing required fields, invalid field values, duplicate supporters or an introducer who is not in the file can cause this warning.

Startup does not overwrite the unreadable file. Copy it somewhere safe before running another command if you want to recover its contents. Even `list` saves data after executing, so it can replace the file with the empty list. A command that fails parsing or validation does not overwrite it.

The next command's result or error replaces the startup warning in the display. To repair the file, close the app, correct the copied data using the rules below, and restore it before relaunching.

### Editing the data file

SupporterBook data is saved as a JSON file `data/supporterbook.json`, relative to the folder the app is launched from. Advanced users can edit that file while the app is closed.

Each supporter in the data file has a `"stage"`, which records how far the relationship has progressed. It must be one of `Prospect`, `Contacted`, `Cultivating`, `Giving`, `Lapsed` or `Declined` (upper or lower case). A supporter's stage stays the same when you edit their other details.

A supporter's `"phone"`, `"email"` and `"address"` keys can each be left out if the supporter does not have one, but every supporter must have at least one of `"phone"` and `"email"`.

A supporter may also have an `"organisation"`, such as the company a CSR contact works for. Leave the key out if the supporter has no organisation. If present, it must not be blank and must be at most 100 characters long.

Each supporter card in the list shows the supporter's organisation, or `(no organisation)` if they have none.

A supporter may also have an `"introducer"`, the name of the supporter who introduced them, e.g. a board member. Leave the key out if no introducer is recorded. If present, it must be the name of another supporter in the same file (capital letters and extra spaces are ignored), and cannot be the supporter's own name.

A supporter's `"interactions"` array stores their interaction history in logging order. Each entry has a `"date"` in `yyyy-MM-dd` format and a non-blank `"note"` of at most 500 characters. For example:

```json
"interactions" : [ {
  "date" : "2026-10-11",
  "note" : "Called to discuss the year-end appeal"
} ]
```

The array may be empty or left out when the supporter has no interactions. A missing array is also how data files created before interaction histories were introduced remain compatible.

<box type="warning" seamless>

**Caution:**
If your changes make the data file invalid (e.g., a supporter's `"stage"` is missing or is not one of the six stages), SupporterBook starts with an empty list and shows the startup warning described above. Back up the file before editing it, and preserve the invalid file before running another command if you need to repair it.<br>
Edit the data file only if you are confident that you can follow the validation rules.
</box>

---

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous SupporterBook home folder.

---

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

---

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add n/NAME [p/PHONE] [e/EMAIL] [a/ADDRESS] [o/ORGANISATION] [st/STAGE] [t/TAG]... ` <br> e.g., `add n/Tan Wei Ming p/91234567 e/weiming@dbs.com o/DBS Bank st/contacted t/boardIntro`
**Clear**  | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit**   | `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [o/ORGANISATION] [t/TAG]... `<br> e.g., `edit 2 p/91234567 a/1 HarbourFront Avenue`
**Intro**  | `intro INDEX by/[INTRODUCER_INDEX]`<br> e.g., `intro 3 by/1`
**Find**   | `find KEYWORD [MORE_KEYWORDS]...`<br> e.g., `find tan wei`
**List**   | `list`
**Log**    | `log INDEX nt/NOTE`<br> e.g., `log 2 nt/Called to discuss the year-end appeal`
**Stage**  | `stage INDEX STAGE`<br> e.g., `stage 2 cultivating`
**Stats**  | `stats`
**View**   | `view INDEX`<br> e.g., `view 2`
**Help**   | `help`
**Exit**   | `exit`

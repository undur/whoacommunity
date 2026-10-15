# What the WebObjects mailing lists asked for help with, 2007–2026

Mined from the local mbox export of webobjects-dev (Apple and WOCommunity), webobjects-deploy, wonder-disc and woproject-dev on 2026-09-05. Report artifact: https://claude.ai/code/artifact/420fa95e-1f99-44d8-89ed-4a98adea2727

| | |
|---|---|
| Messages | 75,832 |
| Threads | 13,728 |
| Answered questions (social threads excluded) | 7,398 |
| Span | 2007–2026 |
| Questions since 2013 | 1,638 |

## The shape of the archive

Question volume fell tenfold between 2009 and 2014 and has run at a few dozen threads a year since 2018. The old volume is dominated by WOLips and Eclipse breakage that no longer applies; the recent trickle is almost entirely about keeping WebObjects running on current Java, current operating systems, Maven, and deployment. Both matter: the recent questions say what people hit today; the old ones say what they will hit again the moment they touch EOF, because the framework has not changed.

Question threads per year: 2008 1,725 · 2009 1,552 · 2010 1,027 · 2011 700 · 2012 686 · 2013 431 · 2014 272 · 2015 222 · 2016 170 · 2017 101 · 2018 68 · 2019 51 · 2020 68 · 2021 55 · 2022 23 · 2023 28 · 2024 43 · 2025 52 · 2026 20 (to September)

### What people ask now (2018 onwards)

- Maven: the main bundle not found when running from Eclipse, compiling with Maven, JDBC drivers as dependencies, archetypes on maven.wocommunity.org
- Java and OS upgrades: JDK 9 and 10, "Java 17? Half or Full?", Monterey installs, WOInstaller.jar being out of date
- Deployment: a deployment script, WOMonitor on CentOS, Apache module issues, 403 Forbidden from the adaptor, how often to bounce instances
- Eclipse and WOLips breaking on each Eclipse release, and WOLips development itself
- EOF: "Cannot determine primary key for entity", ERXEC and the shared editing context, unit tests with EOF, Postgres "idle in transaction"
- Modern UI kits, Bootstrap, and Prototype.js showing its age

## Recurring problem areas

Answered question threads per area; a thread can fall into two areas.

| Problem area | Threads | 2013+ | 2018+ |
|---|---:|---:|---:|
| WOLips and Eclipse: install, upgrade breakage, editors | 1,423 | 218 | 64 |
| EOF: faults, "cannot obtain globalID", uninitialized objects, snapshots | 784 | 164 | 45 |
| Installing WebObjects itself: WOInstaller, Windows, Linux | 477 | 158 | 52 |
| Database drivers and adaptors: JDBC, MySQL, Postgres, SQL Server, FrontBase, Oracle | 640 | 133 | 34 |
| Java version upgrades (8, 11, 17, 21+) and OS upgrades breaking WO | 315 | 131 | 50 |
| D2W: rules, look, customizing pages | 549 | 158 | 27 |
| Entity Modeler / EOModeler and SQL generation | 641 | 116 | 32 |
| Ajax: update containers, modal dialogs, submit buttons, Prototype/jQuery | 717 | 107 | 31 |
| Memory, performance, thread deadlocks and hangs | 593 | 130 | 29 |
| Deployment: wotaskd, JavaMonitor, instances not starting, lifebeat | 426 | 118 | 32 |
| Maven: building, archetypes, repositories, bundles | 350 | 85 | 41 |
| File upload and download, large files, streaming responses | 469 | 99 | 29 |
| EOF: saving, validation and delete rules; relationships not saving | 456 | 108 | 21 |
| Direct actions, URL generation, cookies, redirects | 329 | 67 | 20 |
| Classpath, frameworks and NoClassDefFoundError | 298 | 49 | 17 |
| Apache adaptor: building mod_WebObjects, Apache 2.4, rewrite, 403/500 | 159 | 60 | 20 |
| REST: ERRest, JSON, web services | 288 | 67 | 9 |
| Dates, time zones and NSTimestamp | 141 | 41 | 14 |
| Components, bindings and templates: unknown key, WOD parse errors, inline bindings | 196 | 27 | 7 |
| Primary keys, sequences and "Cannot determine primary key" | 144 | 36 | 5 |
| Localization and character encoding | 165 | 23 | 4 |
| Sessions: timeouts, session store, "backtracked too far", Ajax and sessions | 118 | 18 | 5 |
| Mail: ERJavaMail and SMTP | 56 | 7 | 1 |
| Optimistic locking failures | 14 | 4 | 0 |

Most-mentioned exception names across question threads: IllegalStateException (152 threads), NullPointerException (141), IllegalArgumentException (116), NSForwardException (109), NoClassDefFoundError (78), EOGeneralAdaptorException (57), ClassCastException (41), ClassNotFoundException (35), UnknownKeyException (26), OutOfMemoryError (18). The first four are EOF and request-handling misuse; NoClassDefFoundError is nearly always a classpath or build problem; EOGeneralAdaptorException is the database.

## Guides the site should carry, in priority order

Ranked by recent demand first, then total volume, and checked against what whoacommunity.com already publishes.

### 1. Deploying WebObjects applications today — covered on the site

The three deployment guides answer this directly, and the modulo path removes the Apache adaptor questions entirely. Worth a troubleshooting section: instance won't start from JavaMonitor, "No Instance Available", lifebeat and port conflicts.

**Deployment: wotaskd, JavaMonitor, instances not starting, lifebeat** — 426 threads, 118 since 2013, 32 since 2018
- 2014: Can't start app from javamonitor (25 msgs, 6 people)
- 2018: Deployment script (21 msgs, 6 people)
- 2013: Your request produced an error. (21 msgs, 8 people)
- 2013: Nightmare of deployment again (18 msgs, 6 people)

**Apache adaptor: building mod_WebObjects, Apache 2.4, rewrite, 403/500** — 159 threads, 60 since 2013, 20 since 2018
- 2014: Precompiled WOAdaptor for apache 2.4 (30 msgs, 12 people)
- 2017: Looking for a 64 bit WOAdaptor for Apache 2.4 (22 msgs, 5 people)
- 2013: Java Monitor configuration not working under windows (16 msgs, 5 people)
- 2014: Application not start in womonitor (10 msgs, 6 people)

### 2. Running WebObjects on a current JDK — partly covered

The JDK 25 and JDK 26 articles and wonder-slim cover the answer, but as news items. One guide should state the rule: which JDK, the --add-opens set, the GetPropertyAction workaround or wonder-slim, and what breaks at each version boundary. Strongest growth topic since 2018.

**Java version upgrades (8, 11, 17, 21+) and OS upgrades breaking WO** — 315 threads, 131 since 2013, 50 since 2018
- 2014: Precompiled WOAdaptor for apache 2.4 (30 msgs, 12 people)
- 2017: Compiling ERExtensions in Sierra no worky (26 msgs, 9 people)
- 2014: Convert WebObjects app to pure java (20 msgs, 7 people)
- 2014: Java 8 (18 msgs, 10 people)

### 3. Setting up a WebObjects development environment in 2026 — partly covered

The wonder-slim development guide covers the tooling half. Missing: the WebObjects frameworks themselves. With Maven and the WOCommunity repository there is no WOInstaller step, and the guide should say so, because "WOInstaller.jar needs update" and "install on Monterey" are what people still search for.

**Installing WebObjects itself: WOInstaller, Windows, Linux** — 477 threads, 158 since 2013, 52 since 2018
- 2013: Nightmare of deployment again (18 msgs, 6 people)
- 2013: centos 6.2 - pulling my hair out (PARTIALLY SOLVED) (15 msgs, 7 people)
- 2013: Java Monitor configuration not working under windows (16 msgs, 5 people)
- 2013: Unable to upgrade Oracle jdbc driver (20 msgs, 4 people)

**WOLips and Eclipse: install, upgrade breakage, editors** — 1,423 threads, 218 since 2013, 64 since 2018
- 2014: WOLips for Eclipse 4.3.1 (29 msgs, 16 people)
- 2014: Eclipse Luna (4.4.1) and WOLips: so close (25 msgs, 11 people)
- 2013: My first Hell World App (17 msgs, 9 people)
- 2015: Entity Modeler SQL generation exception (16 msgs, 6 people)

### 4. Building WebObjects applications with Maven — partly covered

The builds article covers vermilingua. Still needed: main bundle not found when running from Eclipse, archetypes, the WOCommunity repository, framework dependencies, and the NoClassDefFoundError family, which is almost always a classpath problem in disguise.

**Maven: building, archetypes, repositories, bundles** — 350 threads, 85 since 2013, 41 since 2018
- 2015: Using WO with modern dependency management (19 msgs, 11 people)
- 2025: Can’t seem to know main bundle when running maven version in Eclipse (25 msgs, 3 people)
- 2014: maven.wocommunity.org is down (12 msgs, 6 people)
- 2015: No template found for component Main (15 msgs, 4 people)

**Classpath, frameworks and NoClassDefFoundError** — 298 threads, 49 since 2013, 17 since 2018
- 2013: Postgresql connection issue (19 msgs, 6 people)
- 2015: Build Path issue - unbound Wonder frameworks (12 msgs, 5 people)
- 2013: WebObjects 5.3.4 Frontbase question (9 msgs, 5 people)
- 2018: Looking towards JDK 9, 10 (7 msgs, 5 people)

### 5. EOF in practice: faults, editing contexts and saving — not covered

The largest technical topic in the archive and the one with no coverage on the site. Why an object "has not been initialized", "cannot obtain globalID", editing context nesting and locking, saveChanges failures, relationships that don't persist, delete rules.

**EOF: faults, "cannot obtain globalID", uninitialized objects, snapshots** — 784 threads, 164 since 2013, 45 since 2018
- 2014: EOCustomObject valueForKey avoiding infinite recursion (20 msgs, 6 people)
- 2013: Is it safe to use NSDictionary as the objects in a (WO/ERX)DisplayGroup? (18 msgs, 7 people)
- 2013: d2w query question (17 msgs, 6 people)
- 2016: Question about jdbc connection pool (14 msgs, 8 people)

**EOF: saving, validation and delete rules; relationships not saving** — 456 threads, 108 since 2013, 21 since 2018
- 2014: EOCustomObject valueForKey avoiding infinite recursion (20 msgs, 6 people)
- 2016: How to avoid expensive fetches in database when setting to-one-relationship (16 msgs, 6 people)
- 2013: build directory issue - (15 msgs, 6 people)
- 2013: help with D2W target (15 msgs, 9 people)

**Primary keys, sequences and "Cannot determine primary key"** — 144 threads, 36 since 2013, 5 since 2018
- 2013: Duplicate entry '4183884' for key 'PRIMARY' (19 msgs, 9 people)
- 2013: @Override of EO? (17 msgs, 8 people)
- 2015: Preferred way for fetching with given SQL select statement? (10 msgs, 6 people)
- 2014: exposing the primary key? (10 msgs, 7 people)

### 6. Database drivers and connections for WebObjects — not covered

Which JDBC driver versions work with the EOF adaptor, the 2021 PostgreSQL driver upgrade question, MySQL 8, SQL Server, connection pooling and "idle in transaction", EOGeneralAdaptorException diagnosis.

**Database drivers and adaptors: JDBC, MySQL, Postgres, SQL Server, FrontBase, Oracle** — 640 threads, 133 since 2013, 34 since 2018
- 2013: Postgresql connection issue (19 msgs, 6 people)
- 2013: Duplicate entry '4183884' for key 'PRIMARY' (19 msgs, 9 people)
- 2013: mysql access exception after a long time (16 msgs, 9 people)
- 2016: Question about jdbc connection pool (14 msgs, 8 people)

### 7. Diagnosing a slow, hung or leaking instance — not covered

Thread dumps, heap size, session counts, the "instance becomes unresponsive after 12 hours" pattern, deadlocks between editing contexts. A companion to the deployment guides and wonder-slim-deployment's monitoring.

**Memory, performance, thread deadlocks and hangs** — 593 threads, 130 since 2013, 29 since 2018
- 2014: Eclipse Luna (4.4.1) and WOLips: so close (25 msgs, 11 people)
- 2014: D2W Problem in new application (17 msgs, 5 people)
- 2016: Extending the “No Instance Available” timeout (14 msgs, 6 people)
- 2015: Documenting the EO Model (12 msgs, 8 people)

**Sessions: timeouts, session store, "backtracked too far", Ajax and sessions** — 118 threads, 18 since 2013, 5 since 2018
- 2016: Extending the “No Instance Available” timeout (14 msgs, 6 people)
- 2015: Odd backtracking error (9 msgs, 5 people)
- 2018: WOCookie after browser restart? (8 msgs, 4 people)
- 2018: Session Cookies (8 msgs, 3 people)

### 8. Entity Modeler, migrations and SQL generation — not covered

Highest raw thread count of any tool topic. Much is WOLips-era breakage that Parslips sidesteps, but SQL generation, migrations and the model file format are independent of the editor and still asked.

**Entity Modeler / EOModeler and SQL generation** — 641 threads, 116 since 2013, 32 since 2018
- 2015: Entity Modeler SQL generation exception (16 msgs, 6 people)
- 2013: Deploying from Mavericks, now wants index.eomodeld / eomodel.plist (15 msgs, 6 people)
- 2015: Framework EOModels (16 msgs, 4 people)
- 2019: EOF and MS SQLServer? (10 msgs, 7 people)

### 9. Uploading and downloading large files — not covered

Streaming a WOResponse, WOFileUpload limits, ERAttachment, the 5.4 large-upload thread. Small guide, evergreen.

**File upload and download, large files, streaming responses** — 469 threads, 99 since 2013, 29 since 2018
- 2017: Compiling ERExtensions in Sierra no worky (26 msgs, 9 people)
- 2016: What's new? (22 msgs, 14 people)
- 2013: D2W / repetitionComponentName Question (32 msgs, 4 people)
- 2018: Deployment script (21 msgs, 6 people)

### 10. Direct actions, URLs and cookies — not covered

Fully qualified direct-action URLs for email, cookies across restarts, redirects, session IDs in URLs. Asked in every era, answered in one page.

**Direct actions, URL generation, cookies, redirects** — 329 threads, 67 since 2013, 20 since 2018
- 2013: AjaxSubmitButton with directActionRequestHandler results in cryptical class refe (22 msgs, 9 people)
- 2014: D2W Problem in new application (17 msgs, 5 people)
- 2014: Access to EOF/WebObjects source files - plan for the future? (13 msgs, 7 people)
- 2019: WOInstaller.jar needs update (10 msgs, 6 people)

## Method and caveats

Threads were grouped by normalized subject (reply and list prefixes stripped), so a subject reused across years counts once with its message total. A thread counts as a question when its subject or opening lines contain help-seeking language, and only threads with at least one reply were counted. Areas are keyword rules over the subject and the first 500 characters of the opening message; they overlap, and a few social threads leak through despite the exclusion list, so counts are relative weights rather than exact figures. Source: `/Users/hugi/tmp/INBOX/Postlistar/wo-lists.mbox/mbox`; the `table_of_contents` next to it is Mail.app's index and was not needed.

# Sweranker #

Sweranker is an Android Application whose goal is to to show analytics of some of Portugal's most well reputed Informatics Engineering degrees regarding their conformity to the Software Engineering Body of Knowledge (SWEBOK).
Most of the portuguese University Degrees regarding Software Engineering are called Informatics Engineering. This designation stemmed from the french term "Information Automatique" (from Automatic Information). It came from a
time when the portuguese culture was heavily influenced by the french one due to the familiarity ties between the two countries and it stuck throughout the time.

Informatics Engineering, of course, is a nonexistant umbrella term in the modern world. What in Portugal is known as Informatics Engineering is actually called, in the widely accepted terminology, Software Engineering.
Software Engineering is then, the name given to the Human Knowledge Discipline that regards the Engineering branch that deals with Software.
As any other Human Knowledge Discipline it has a well defined, restricted Body of Knowledge. That is, a generally accepted standard that meticullously defines all the Knowledge that together qualifies an individual as a
Software Engineer.

More information about this standard can be found here: https://www.computer.org/web/swebok/v3-guide

Download it from Google Play here (currently BETA): https://play.google.com/apps/testing/pt.cmg.sweranker

### What does this app do? ###

This app's main goal is to crunch through every available degree it has and calculate a score.
This score is based on how well each degree combination (a combination can be seen as "one possible way to complete a given degree") fares against the SWEBOK.
Since the SWEBOK is literally the "Software Engineering Book of Knowledge" this calculated score is translated into a numerical representation of HOW WELL
each degree covers the SWEBOK topics, or in simpler words, how much Software Engineering each of these degrees have.

This app's features:
- lightweight guide of the SWEBOK. It has a quick-reference, compressed guide to the Software Engineering Body of Knowledge
- Software Engineering Degrees' guide. Six of the most well-reputed portuguese Software Engineering degrees with their complete program.
- graphical view of each degree's strenghts and weaknesses. Each degree is ranked against the SWEBOK to find out their worth in Software Engineering terms.

### The main menu ###

The main screen of this app shows a random Degree Combination Score. Currently, at setup there will be NO score displayed.
This happens because before any score can be shown it must be calculated beforehand. At the moment each degree score must be calculated in app, for each installation.

![Main Menu image](/images/MainMenu.png)

There are 3 options to select from this navigation menu:

*   SWEBOK
*   Curricula
*   Rankings



### The SWEBOK ###

This section of the application is dedicated to a brief, comprehensible summary of what the SWEBOK is all about. The
SWEBOK is a book that contains what is known as the Software Engineering Body of Knowledge.
This Body of Knowledge is the set of all the Areas of Human Knowledge that together define what a person should master in
order to be considered a Software Engineer. In simpler words, the Body of Knowledge describes what Software
Engineering is.

For example, the Body of Knowledge of General Medicine would probably be composed of all the traditional branches of that science, such as 
Pharmacology, Endocrinology, Toxicology and so on. 

Ideally, there should be a Body of Knowledge for every possible professional activity, it is a way to structurally classify the whole of the Human Knowledge.
However, most professional associations don't bother to define one, either because there is no consensus of what the generally accepted Body of Knowledge
should be or, more commonly, because they don't feel the need to write a formal documentation of one. Shame.

The SWEBOK area of this app is composed of a grid of all the 15 Areas of Knowledge of Software Engineering and by clicking each one a brief
explanation of it is loaded, as well as the topics that compose it. This is mainly a bibliographic effort.

![SWEBOK area](/images/SWEBOK_main.png)



### Curricula ###

The Curricula section is where all the Degrees and their curricula descriptions are available for browsing.
The first thing to be seen is a grid with all the available portuguese Degrees. By clicking on one Degree, a detailed description of
it as well as its full curriculum are displayed. This view details a Degree until the very complete subjects of each Degree Class, with its
full program for the year.

This a very important screen in the app. This is the very point of this app, the degrees and how each of them fares when compared to the
SWEBOK. There is a number of degrees available in this app, at this stage (BETA) one is available and 5 more are on the way.
These degrees are not randomly chosen, they are the very best in Portugal and they are ALL taught at public universities. Since they are the most
relevant of the country it is of the upmost interest to classify them and hopefully see their relative worth on what concerns to Software Engineering.

None of them is known as Software Engineering because sadly, that name is not yet widely used in Portugal.
Most are known as Informatics Engineering, but to be clear, in terms of the international community _there is no such thing._
Informatics is just an older name for the subject, before the term Software Engineering was widespread, and derives from the French term "Information Automatique".

![Degree example](/images/Degree_example.png)

### The Scores Area ###

The Scores are is the very core of this app and the purpose of it.
When the user selects this option he is greeted with a random choice of 10 different, ordered degree combinations' scores per degree.
In other words, there will be 10 scores for each different degree in the app. They are free to explore in themselves, but it is more useful
to instead use the filters to actually search for some criteria-based scores.

![Scores main menu](/images/scores_main.png)

#### Degree Combination ####

An important concept to understand this section is a _Degree Combination._

Every Score given to a degree is not given to it directly as a whole, unique, definitive value. This happens because most Degrees, if not all,
are not linear, i.e. for every Degree there is usually more than one way to complete it (most likely, a lot more ways). Most degrees have Optional
classes that one can take (you can chose from a given set of them, for example) and a lot of Degrees have specialisation branches that are mutually
exclusive. This in itself effectively splits a Degree into many possible Degree Combinations. So, a Degree Combination is nothing more
than a specific set of Degree Classes that a person can take to complete a degree. So in reality, there is not single score for one degree, but rather
a **single score per Degree Combination, i.e. per different way to complete a degree.**

Every Degree Combination is unique, meaning that there is **at least** one class that differs it from any other combination.

This gives potential to a lot of interesting conclusions for a given degree, because it surfaces specific strenghts and weaknesses to a combination.
Do you want your degree to focus in Software Construction? Then "Combination nº X" from a given degree is probably more suited to your needs
because it has more Software Construction classes than, say "Combination nº Y" that is more focused on Software Testing. This app lends itself to this
kind of conclusion - that is the point of this app.

#### The filters ####
These are simple practical filters that allow for a refined ordering of the scores based on their highest relative proportion of coverage for
each of the 15 different Knowledge Areas.

![Scores filter](/images/scores_filter.png)

### Individual Score ###
An individual score of a given Degree Combination is a graphical representation of all the metrics that describe the compliance to SWEBOK of a given
combination. This is shown by means of a set of charts as it is a great way to visualise the information in a meaningful way. Currently there are only 
3 metrics, namely:
*   the coverage percentage of each Knowledge Area
*   the weighted avergage for each Knowledge Area in the degree
*   the absolute counters for each KA and Topic in the degree

The first is pretty straightforward: given the whole of 15 Knowledge Areas and all its composing 102 Knowledge Area Topics, does that particular
degree combination cover all of them at any point in its curriculum?

![Score Percent](/images/score_1.png)

Then the weighted average: in the whole curriculum for this combinaton **how much** of each Knowledge Area is covered compared to he universe of the 
15 Areas? This metric is what gives the sense of proportion in the degree. It answers the question: what is the main focus of this degree?
(Note: every mean calculated is actually a weighted average. This just means that more important classes contribute more to the average than the less important
ones. This is only natural, if a class is given 5 hours per week and another is just 2 hours per week, the first has to be more taken into account).

![Score Average](/images/score_2.png)

Finaly the counters: this is simply the number of times each Knowledge Area, via the cummulative counters of each of its topics, is matched in this degree's 
classes curriculum. Altough less valuable than the weighted average it still gives an interesting view on the particular focus of a given combination.

![Score Counters](/images/score_3.png)
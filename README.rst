====
Baza
====

Baza is a scraper for data from `Museumplan.nl`_ that publishes to Google
Calendar. The code was written as a (pro bono) late night hacking project to
help the staff at `Museum Beelden aan Zee`_ make their internal schedule
available through Google Calendar. This means that the application itself is
only relevant to Museum Beelden aan Zee (and possibily other Museumplan.nl
users), but the source code is available to other developers who are working
on Google Calendar integration or `Enlive`_ scraping in `Clojure`_. As a late
night hacking project this application wasn't written with elegant example
code in mind. The unit tests in particular are a bit lacking (only the
functional part of the code is covered), because I felt the use case didn't
warrant writing extensive mocks of Google Data API or scraping results from
Enlive. That being said, I hope the code will be useful to someone working on
a similar project.

Installation
============

As a user, you only need to download the file
`baza.jar`_ (which includes all required libraries)
and store it somewhere on your filesystem (e.g. ~/bin in your homedirectory on
UNIX or C:\\ on Windows). Clojure uses the Java Virtual Machine as a host
platform, so a copy of the JVM is required to run the application.

Usage instructions
==================

There is no graphical interface because the application is intended to be
usable in a server environment. The best way to run it is through a UNIX
cronjob or alternatively a "Scheduled Task" on Windows (I don't have access to
a Windows machine for testing, but it seems like there is a good description
of the process on the `relevant Microsoft Support page`_).

Here is the command to execute the application:

    java -jar /path/to/baza.jar
    --google-username "your.address@gmail.com"
    --google-password "your.password"
    --calendar-uri "https://www.google.com/calendar/feeds/your.address@gmail.com/private/full"
    --mp-username "username"
    --mp-password "password"
    --mp-uri "http://www.museumplan.nl/mbaz/partners/index.php"

Nota bene: the line breaks were added for readability and should be removed;
the command itself is one big line! The options are separated by spaces.  When
copy and pasting, make sure these spaces are preserved (otherwise the
application will return an "Unknown option" error).

To use the application you should edit the command and add your own login for
Museumplan.nl and Google Calendar. The
"/path/to/baza.jar" value should be changed to
reflect the location of the file on your filesystem (e.g.
~/bin/baza.jar on UNIX or
C:\\baza.jar on Microsoft Windows).


Feel free to contact me if you have any questions.

Copyright 2011, F.M. (Filip) de Waard (fmw@vix.io).
Distributed under the `Apache License, version 2.0`_.

.. _`Museumplan.nl`: http://www.museumplan.nl/
.. _`Museum Beelden aan Zee`: http://www.beeldenaanzee.nl/
.. _`Clojure`: http://clojure.org/
.. _`Enlive`: https://github.com/cgrand/enlive
.. _`baza.jar`: https://github.com/downloads/fmw/baza/baza.jar
.. _`Leiningen`: https://github.com/technomancy/leiningen
.. _`relevant Microsoft Support page`: http://support.microsoft.com/kb/313565
.. _`Apache License, version 2.0`: http://www.apache.org/licenses/LICENSE-2.0.html

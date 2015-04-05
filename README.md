# SPARK

SPARK brings modern web-development to the Atlassian platform. It adds support for JS-based front-end development 
tools (Gulp, LiveReload, ...), and for web-frameworks to build UIs as single page applications (AngularJS, 
ReactJS, ...).

At development time, front-end developers can choose JS-based build tools (Grunt, Gulp, etc.) and other helpers 
(JSHint, Karma, LiveReload, etc.), that support their development workflow best. And they can integrate those 
tools in the existing, Maven-based build from the Atlassian SDK, which minimizes the impact on back-end developers 
and on existing build infrastructure.

At runtime, SPARK takes care of deploying the SPAs within the add-on, and integrates the SPAs in the UI of the 
host application (JIRA, Confluence, ...). Therefore add-on developers can integrate SPAs as the following types:
* a Dialog App runs the SPA in a modal dialog,
* an Admin App runs the SPA as a page in the global admin UI.
* a Space App (cool name, eh?) runs the SPA as a page in space-level UI (Confluence-only).

In short, SPARK is a Single Page Application FramewoRK for the Atlassian add-ons (which also explains the name).


## Documentation
Find all the Documentation at https://www.k15t.com/display/SPARK.
                                 

## License

Licensed under The MIT License (MIT).

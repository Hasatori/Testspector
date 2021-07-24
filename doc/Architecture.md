# Architecture description

## General architecture description

The solution is based on the MVC architecture: 
 * The classes for rendering information to the user are stored in the ``com.testspector.view`` package.
 * All logic for checking best practices is stored in the ``com.testspector.model`` package.
 * The two layers are then controlled and linked using classes stored in the ``com.testspector.controller`` package

![architecture detail](./Class_diagram_global_detailed.png)

### Detail architecture design

![architecture detail](./Class_diagram_global_detailed_factories.png)

## Architecture implementation for the JUnit testing framework

![architecture detail](./Class_diagram_global_detailed_junit.png)

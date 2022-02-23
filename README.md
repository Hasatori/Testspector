# Testspector ![Generic badge](https://img.shields.io/github/license/Hasatori/Testspector)  ![Version](https://img.shields.io/jetbrains/plugin/v/16300-testspector)


Testspector is an IntelliJ IDEA plugin that via code inspections helps developers with writing unit tests. It contains a dataset of best practices that should be followed when writing unit tests. 
The unit tests are checked against the dataset and any violation is reported to the user. The user is provided with a description of the problem, hints and also actions for fixing the issue. 
Description also contains links to the documentation where are all best practices described with examples.

Plugin URL: https://plugins.jetbrains.com/plugin/16300-testspector

## Documentation

 Detailed documentation of all best practices and also plugin architecture is available on the following url - https://www.testspector.com/

## Supportability

The plugin currently supports following IntelliJ products:
* IntelliJ IDEA Educational — 2019.3 — 2021.1.3

* IntelliJ IDEA Ultimate — 2019.3 — 2021.2

* IntelliJ IDEA Community — 2019.3 — 2021.2

* Android Studio — build 193.0 — 213.0

# Install
Install the plugin by going to ``Settings -> Plugins -> Browse repositories`` and then search for **Testspector**.

# Configuration
Inspections are part of the IntelliJ IDEA inspections and can be configured by going to ``Settings -> Editor -> Inspections``.  All inspections are in the group **"Testspector"**
and each inspection is also categorized into a separate group and subgroup based on to which best practice it checks (see ![Documentation](./doc/Practices.md)).
Every inspection or group with inspections is configurable - it is possible to decide if it will be enabled or disabled and severity and scope, in which it will run, can be set as well.
By default, all inspections are enabled and the severity level is set to **Warning**.

![configuration.png](./doc/configuration_general_settings.png)

It is possible to suppress inspections for a certain part of the code. For example for methods or classes. For that purpose annotation ``@SuppressWarnings({"INSPECTION_NAME"})`` can be used. An example for suppressing inspection **AT_LEAST_ONE_ASSERTION** can be seen below.

![configuration_without_suppress.png](./doc/configuration_without_suppress.png)
![configuration_with_suppress.png](./doc/configuration_with_suppress.png)

# Usage

By default inspections are invoked automatically once user opens a file or makes some changes to it. Identified problems are highlighted in the code and if user hovers them description of the problem is provided. Except for descriptions there are sometimes hints how to solve the issue and in some cases use is even provided with action which will automatically fix the issue. 
Description also contains link to the  ![Documentation](./doc/Practices.md) where is broken best practice described in detail. 
Example can be seen below. There is a test that contains redundant try catch block. 

![usage_try_catch_problem_description.png](./doc/usage_try_catch_problem_description.png)

Catching the exception is not part of the test, it makes test harder to read and makes it longer. It can be removed and exception can be caught at method level. 

![usage_try_catch_action_to_fix.png](./doc/usage_try_catch_action_to_fix.png) ![usage_try_catch_fixed.png](./doc/usage_try_catch_fixed.png)

## Invoke inspection manually

Inspections can be invoked manually two different ways:

*  Using project view popup menu and selecting any file or folder:
   
![usage_side_window.png](./doc/usage_side_window.png)
*   Using editor popup menu in any file
    
![invoke_manually_file_popup_menu.png](./doc/invoke_manually_file_popup_menu.png)


Inspection runs in the background and detected problems are over time added to the report.

![invoke_manually_inspections_report.png](./doc/invoke_manually_inspections_report.png)

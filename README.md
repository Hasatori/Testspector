# Testspector ![Generic badge](https://img.shields.io/github/license/Hasatori/Testspector)  ![Version](https://img.shields.io/jetbrains/plugin/v/16300-testspector)


Testspector is an IntelliJ IDEA plugin that helps developers with writing unit tests. It contains a dataset of best practices that should be followed when writing unit tests. The unit tests are checked against the dataset and any violation is reported to the user. The user is provided with a description of the problem, hints on how to solve it and also parts of code causing the problem. The report also contains links to the documentation where are all best practices described with examples.

Plugin URL: https://plugins.jetbrains.com/plugin/16300-testspector
## Documentation

### Best practices 
The dataset of best practices was created by a detailed analysis of the world’s top books and studies regarding unit testing. 

* ![Documentation](./doc/Practices.md)

Current version of the plugin supports checking of the following 7 best practices for [JUnit 5](https://junit.org/junit5) and [JUnit 4](https://junit.org/junit4):

* [Test only public behaviour](./doc/Practices.md#test-only-the-public-behaviour-of-the-tested-system)
* [At least one assertion](./doc/Practices.md#at-least-one-assertion-per-test)
* [Only one assertion](./doc/Practices.md#only-one-assertion-per-test)
* [No global static properties](./doc/Practices.md#do-not-use-global-static-properties)
* [Set up a test naming strategy](./doc/Practices.md#setup-a-test-naming-strategy)
* [Catch tested exceptions using framework tools](./doc/Practices.md#catch-tested-exceptions-using-framework-or-library-tools)
* [No conditional logic](./doc/Practices.md#do-not-use-if-switch-for-or-while-blocks-in-a-test)

### Architecture
The architecture of the plugin was designed in a way that supports implementation for any unit testing framework that is currently supported by the IntelliJ IDEA platform.
* [Architecture](./doc/Architecture.md) 

## Supportability

The plugin currently supports IntelliJ IDEA ultimate versions 2019.2 (192) - 2020.3.3 (211)

# Install
Install the plugin by going to ``Settings -> Plugins -> Browse repositories`` and then search for **Testspector**.

# Usage
1. Invoke inspection:
   * Using side navigation window and selecting any file or folder:

      ![usage_side_window.png](./doc/usage_side_window.png)
   * Based on unit testing framework and programming language inspection can be invoked on a single test or object that contains them. For example for JUnit by clicking on a icon next to the test class or test method declaration

      ![usage from file.png](./doc/usage_from_file.png)

2. Report showing violated best practices is generated. In some cases there are hints suggesting how the problem can be solved. If you need more information about the best practice or want to check out other best practices there is also a link to the documentation.

      ![usage_report.png](./doc/usage_report.png)

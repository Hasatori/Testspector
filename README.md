# Testspector ![Generic badge](https://img.shields.io/github/license/Hasatori/Testspector)  ![Version](https://img.shields.io/jetbrains/plugin/v/16300-testspector)


Testspector is an IndelliJ IDEA plugin which helps developers with writing unit tests. It contains a dataset of best practices which should be followed when writing unit tests. The unit tests are checked against the dataset and any violation is reported to the user. User is provided with description to the problem, hints how to solve it and also parts of code causing the problem. 
Report also contains links to the documentation where are all best practices described with examples (the documentation can be found here: ![Best practices](./doc/Practices.md) - The documentation is only in czech. Translation is in progress).

Plugin URL: https://plugins.jetbrains.com/plugin/16300-testspector

### What do you think of the plugin?
Please let me know by filling this survey: https://www.survio.com/survey/d/R2L3F8O5P2S0F7I2C (**very short 10 min max, just 8 questions where 6 mandatory**).  This would help me a lot, since it will show me if I am headed the right direction and the plugin is useful.

## Documentation

### Best practices 
The dataset of best practices was created by detailed analysis of the world’s top books and studies regarding unit testing. The documentation can be found here: ![Best practices](./doc/Practices.md) (Only in czech at the moment. Translation is in progress). There are direct references to chapters in books and studies where the best practices were derived from. 
### Architecture
![Architecture](./doc/Architecture.md) (Only UML class diagrams are in english, rest is in czech - translation is in progress) of the plugin was designed in a way that it supports implementation for any unit testing framework that is currently supported by IntelliJ IDEA platform.

Current version of the plugin supports checking of the following 7 best practices for ![JUnit 5](https://junit.org/junit5) and ![JUnit 4](https://junit.org/junit4):

* ![Test only public behaviour](./doc/Practices.md#testovat-pouze-verejne-chovani-testovaneho-systemu)
* ![At least one assertion](./doc/Practices.md#minimalne-jedna-overovaci-metoda-na-test)
* ![Only one assertion](./doc/Practices.md#prave-jedna-overovaci-metoda-na-test)
* ![No global static properties](./doc/Practices.md#nepouzivat-globalni-staticke-promenne)
* ![Set up a test naming strategy](./doc/Practices.md#urcit-strategii-pojmenovani-testu)
* ![Catch tested exceptions using framework tools](./doc/Practices.md#odchytavat-testovane-vyjimky-pomoci-nastroju-knihoven-ci-testovacich-frameworku)
* ![No conditional logic](./doc/Practices.md#podminena-logika)

## Supportability

Plugin currently supports IntelliJ IDEA ultimate versions 2019.2 (192) - 2020.3.3 (211)

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

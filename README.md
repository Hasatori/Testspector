# Testspector ![Generic badge](https://img.shields.io/github/license/Hasatori/Testspector) 

Testspector is a plugin for checking quality of unit tests. It contains a dataset of 62 best practices which should be followed when writing unit tests. The unit tests are checked against the dataset and any violation is reported to the user. User is provided with description do the problem, hints how to solve it and also parts of code causing the problem. 
Report also contains links to the documentation where are all best practices described with examples (the documentation can be found here: ![Best practices](./doc/Practices.md) - My apolologies but the description of best practices is only in czech language for now. The plugin is result of a czech master thesis, and these parts were copied directly from there. Translation is in progress) 

## Supportability

![Architecture](./doc/Architecture.md) of the plugin was designed in a way that it supports implementation for any unit testing framework that is currently supported by IntelliJ IDEA platform.

A current version of the plugin supports checking of the following 8 best practices for unit testing framework **JUnit** version **4** and **5**:


* ![Test only public behaviour](./doc/Practices.md#testovat-pouze-veřejné-chování-testovaného-systému)
* ![No simple tests](./doc/Practices.md#neimplementovat-jednoduché-testy)
* ![At least one assertion](./doc/Practices.md#minimálně-jedna-ověřovací-metoda-na-test)
* ![Only one assertion](./doc/Practices.md#právě-jedna-ověřovací-metoda-na-test)
* ![No global static constants](./doc/Practices.md#nepoužívat-globální-statické-proměnné)
* ![Set up a test naming strategy](./doc/Practices.md#určit-strategii-pojmenování-testů)
* ![Catch tested exceptions using framework tools](./doc/Practices.md#odchytávat-testované-výjimky-pomocí-nástrojů-knihoven-či-testovacích-frameworků)
* ![No conditional logic](./doc/Practices.md#nepoužívat-bloky-if,-else,-switch,-for-či-while-v-rámci-testu)


# Install
Install the plugin by going to ``Settings -> Plugins -> Browse repositories`` and then search for **Testspector**.


# Usage
1. Invoke inspection:
   * Using side navigation window and selecting any file or folder:

      ![usage_side_window.png](./doc/usage_side_window.png)
   * Based on unit testing framework and programming language inspection can be invoked on a single test or object that contains them. For example for JUnit by clicking on a icon next to the test class or test method declaration

      ![usage from file.png](./doc/usage_from_file.png)

2. Report showing broken best practices is generated. In some cases there are hints suggesting how the problem can be solved. In case when you need more information about the best practice there is also a link to the documentation.

      ![usage_report.png](./doc/usage_report.png)

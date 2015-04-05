# OPLA-Tool

## Description
The OPLA-Tool is a Multi-Objective Optimization Approach for PLA Design (MOA4PLA) developed by Research Group on Software Engineering (GrES) located at Computer Science Department of the Federal University of Paraná (UFPR), Brazil.

## Projects
The OPLA-Tool is composed by following projects:
- Architecture Representation
    - https://github.com/opla-tool/architecture-representation
- OPLA-Patterns
    -  https://github.com/opla-tool/opla-patterns
- OPLA-Core
    - https://github.com/opla-tool/opla-core
- OPLA-Tool
    - https://github.com/opla-tool/opla-tool

You need download all projects before to build the OPLA-Tool.

## Requirements
Before to compile the code, you need to install the following softwares on your PC:
- Java Development Kit (Version >= 6)
- Git - http://git-scm.com
- Maven - http://maven.apache.org

## How to Build
This section show the step-by-step that you should follow to build the OPLA-Tool. 

- Create a directory to build OPLA-Tool:
```sh
mkdir opla-tool
```
- Access the folder:
```sh
cd opla-tool
```
- Download all projects:
```sh
git clone https://github.com/opla-tool/architecture-representation.git
git clone https://github.com/opla-tool/opla-patterns.git
git clone https://github.com/opla-tool/opla-core.git
git clone https://github.com/opla-tool/opla-tool.git
```
- Install dependencies:
```sh
sh architecture-representation/buildDeps.sh
```
- Compile all projects. The sequence is important:
```sh
cd architecture-representation && mvn clean && mvn install
cd opla-patterns && mvn clean && mvn install
cd opla-core && mvn clean && mvn install
cd opla-tool && mvn clean && mvn install
```
- Open OPLA-Tool:
```sh
java -jar opla-tool/target/opla-tool-0.0.1-jar-with-dependencies.jar
```





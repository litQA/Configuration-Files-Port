# Third party notices

## Configuration Files (MCreator plugin)

The mod element definition, the Blockly block definitions, the user interface and the language files of this plugin are
derived from the Configuration Files plugin by NerdyPuzzle. The original work is distributed under the MIT license,
reproduced below.

## Forge Config API Port

On NeoForge and Fabric, the generated mods depend on Forge Config API Port by Fuzs (`forgeconfigapiport`), which is
licensed under MPL-2.0. Forge Config API Port is not redistributed with this plugin. It is resolved as a Gradle
dependency of the generated mod through the `forgeconfigapiport` ModAPI definition shipped with this plugin, using the
Fabric publication on Fabric and the NeoForge publication on NeoForge. Native Forge 1.16.5 to 1.20.1 use the built-in
Forge configuration API and do not depend on the Port.

## Compile time references (`libs/`)

* `libs/mcreator-api.jar` contains the class files of the MCreator 2026.1 runtime. It is used only to compile this
  plugin and is not packaged into the plugin archive. MCreator is a project by Pylo.
* `libs/log4j-api-2.25.3.jar` is the Apache Log4j API, licensed under the Apache License 2.0. It is used only to
  compile this plugin; MCreator provides the Log4j API at runtime.

---

MIT License

Copyright (c) 2026 NerdyPuzzle

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

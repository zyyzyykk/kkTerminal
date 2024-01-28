const { defineConfig } = require("@vue/cli-service");
const MonacoWebpackPlugin = require("monaco-editor-webpack-plugin");
const { languages, features } = require("monaco-editor/esm/metadata");

module.exports = defineConfig({
  transpileDependencies: true,
  chainWebpack(config) {
    config.plugin("monaco-editor").use(new MonacoWebpackPlugin(),[
      {
        languages: [],
        features: [],
      }
    ]);
  },
});
const { defineConfig } = require("@vue/cli-service");

module.exports = defineConfig({
  publicPath: process.env.NODE_ENV === 'production' ? "./" : "/",
  transpileDependencies: true,
  productionSourceMap: false,     // 关闭源映射，减小打包体积，避免暴露源代码
});

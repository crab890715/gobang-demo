<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>五子棋</title>
    <meta name="viewport" content="initial-scale=1, maximum-scale=1">
    <link rel="shortcut icon" href="/favicon.ico">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">

    <link rel="stylesheet" href="./lib/jquery-weui/lib/weui.min.css?r=20160316">
    <link rel="stylesheet" href="./lib/jquery-weui/css/jquery-weui.css">
    <link rel="stylesheet" href="./dist/main.css">
  </head>
  <body ontouchstart>
    <h1>五子棋</h1>
    <p>五子连珠，劳逸结合 - 任我行</p>

    <div class="board-outer">
      <div class="board" id="board">
      </div>
    </div>

    <div class="status">
    </div>

    <div class="buttons">
      <div class="weui-row">
        <div class="weui-col-33">
          <a href="javascript:;" class="weui_btn weui_btn_primary" id='start'>开始</a>
        </div>
        <div class="weui-col-33">
          <a href="javascript:;" class="weui_btn weui_btn_warn" id='fail'>认输</a>
        </div>
        <div class="weui-col-33">
          <a href="javascript:;" class="weui_btn weui_btn_default" id='back'>悔棋</a>
        </div>
      </div>
    </div>

    <footer>
      源码： <a href='https://github.com/lihongxun945/gobang' target="_blank">https://github.com/lihongxun945/gobang</a>
      <br>
      @2016-03-16
    </footer>

    <script src='./lib/jquery-weui/lib/jquery-2.1.4.js'></script>
    <script src='./lib/jquery-weui/js/jquery-weui.js'></script>
    <script src='./dist/main.js?r=20160316'></script>
  </body>
</html>


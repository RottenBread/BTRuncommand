# BTRuncommand
정해진 시간에 명령어가 작동하는 플러그인

# How To Use
```
/plugins/BTRuncommand/run.yml
```
```
# BTRuncommand 파일
# 이 파일에 명령어를 다음과 같이 작성하세요.
tasks:
  - name: "my precious command"
    time: "HH:mm"
    commands:
    - "say it's HH:mm!"
  - name: "clear daily quest"
    time: "HH:mm"
    commands:
    - "quest clear daily"
    - "quest clear weekly"
```

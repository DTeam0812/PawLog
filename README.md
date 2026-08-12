# PawLog

Windows의 활성 창을 기준으로 프로그램 사용 시간을 기록하고 통계를 보여주는 Java 애플리케이션입니다.

## 개발 환경

- Windows
- JDK 24
- Java Swing/AWT
- JNA (`lib` 폴더)
- H2 Database (`lib` 폴더)

## VS Code에서 실행

1. Extension Pack for Java 확장을 설치합니다.
2. `src/ui.Widget.java` 또는 `src/Main.java`를 엽니다.
3. `main` 메서드 위의 **Run**을 누릅니다.

Windows API를 사용하는 `Main` 실행 시 JVM 옵션으로 아래 값을 사용합니다.

```text
--enable-native-access=ALL-UNNAMED
```

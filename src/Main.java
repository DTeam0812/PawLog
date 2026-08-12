import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;//정해진 시간마다 실행해주는 관리자
import java.util.concurrent.TimeUnit;
import java.nio.file.Path; //실행경로 
import com.sun.jna.Native;// windows 문자열을 java 문자열로 변환
import com.sun.jna.platform.win32.User32;//user32.dll(시스템관라) 를 java에서 사용
import com.sun.jna.platform.win32.WinDef.HWND; //window 창을 나타내느 자료형 hwnd 
import com.sun.jna.ptr.IntByReference;

interface TrackerItem {

}
//활성화된 창의 시간만 기록 + 백그라운드 프로그램
//활성화 된 창이 바뀌면? 창의 기록이있는지확인 -> 없으면 새로추가 있으면 기존에서 시간 ++

//db에 무엇을 저장할것인가? 기록 확인은 로컬에서 ? 새 user 가만들어질때마다 new table create 
// 새로운 프로그램 확인되면table에 넣기 table ex) process string / time date  / 

//1. 컴퓨터 사용시간 2. 프로그램 사용시간 3. 활동시간 측정

//

//관리자권한으로 실행.

//무엇을 기준으로 어떻게 보여줄것인가 컴퓨터 켜져있는시간 비례 비율 ? 컴퓨터 동작시간 <<< ? 절대시간  둘다?
class gettracker{
    

}
//scheduledexecutorservice = whilte true 대체제 
//현재 활동중인창 => getforegroundwindow 로 찾아서 => getwindowthreadprocessid 로 pid 탐색,
//포인터로 전달해주기 떄문에 intbyrefernce 로 반환 받아야함
//입력받은 pid로 프로세스 탐색 processid 의 주솟값 안에있는 값을 가져오기위해 .getValue()사용
//ProcessHandle.of(pid)로 pid를 가진 프로세스 탐색 <optional> 항목으로 empty 방지
//processhandle.info 로 정보가져오기 info().command()로 경로 가져옴
//Path.of(경로).getfilemane().tostring()  파일이름(프로세스) 가져오기 문자열로

public class Main {
     private static String previousProgram = null;
     private static long usageSeconds = 0;

     private static final Set<String> Excluded = new HashSet<>();
     static {
          Excluded.add("explorer.exe");
          Excluded.add("ShellExperienceHost.exe");
          Excluded.add("SearchHost.exe");
     }
    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(); //while true 역할

        scheduler.scheduleAtFixedRate(
        ()->{try {HWND windows = User32.INSTANCE.GetForegroundWindow(); //사용자가 보고있는 창  hwnd = 창식별자 HWND 형식반환?
        if (windows == null) { System.out.println("활성 창을 찾지 못했습니다.");  return;}
                
         char[] title = new char[512]; //이름을 담을 배열
         int length = User32.INSTANCE.GetWindowText(windows, title , 512); //GetWindowText(어떤창을? , 어디에? , 최대몇글자?) 반환은int 
        // System.out.println(length);
         String title1 = Native.toString(title); // window  함수는 배열로 반환 -> native 로 자바 문자로 변환
       //  System.out.println(title1);//hwnd -> getwindowprocessid -> pid -> processhandle
         IntByReference processId = new IntByReference(); // windows 에서 변수의 주소를넘김 그래서 int 안씀 , intbyreference = int * 포인터역할 대체
         User32.INSTANCE.GetWindowThreadProcessId( windows, processId); // (hwnd (윈도우창) ,어디에? ) 반환은TID값 PROCESSID에는 PID의 포인터(주소) 저장
        // System.out.println(processId.getValue());//pid값을 얻기위해 getvalue사용

         long pid = processId.getValue() & 0xFFFFFFFFL;// 부호없는 32bit를 양수 정수로

         ProcessHandle.of(pid);//지정된 pid를 가진 프로세스를 찾음
         Optional<ProcessHandle> process = ProcessHandle.of(pid); //optional <-- 있을수도있고 없을수도있다 , <ProcessHandle>형식으로 반환
         if(!process.isPresent() ){
            System.out.println("종료된 프로세스입니다.");
            return;
         }
         ProcessHandle processHandle = process.get();//get 으로 optional 안에 파일을 꺼내온다,
       //  System.out.println(processHandle);

         ProcessHandle.Info info = processHandle.info(); // processhandle의 정보를 가져옴 ,
        
         Optional<String> command = processHandle.info().command(); //t실행결로를 가져옴 .command 반환실패시 optional.empty 로 반환하기떄문
        if (command.isEmpty()) { System.out.println("실행 경로를 가져올 수 없습니다."); return;}
        
         //info.user 사용자 , info.startinstant 시작시간, info.totalCpuDuration(); 누적 cpu사용시간 , info.arguments(); 실행인자.
         String commande = command.get();//타입맞추기
         
         Optional<String>user = processHandle.info().user();
       //  String users = user.get();
         String pathname = Path.of(commande).getFileName().toString();// 실행한 파일이름(프로세스 찾기)
        // System.out.println(users);
        //  System.out.println(info.startInstant());//시작시간
         // System.out.println(info.totalCpuDuration());
         String currentProgram = pathname; 

            if (previousProgram == null) {
            previousProgram = currentProgram;
            usageSeconds = 1;

            System.out.println(
                    "측정 시작: " + currentProgram
            );
            return;
        }

         if(!currentProgram.equals( previousProgram)){ //포커스된 화면 변경
            if (Excluded.contains(currentProgram)) {
               return;
            }
            System.out.println("이전 프로그램 "+previousProgram);
            System.out.println(usageSeconds + "초 실행");
            System.out.println("바뀐 프로그램 "+pathname);
            System.out.println("========================");
            previousProgram = currentProgram;
            usageSeconds = 0;
         }
        else{
             usageSeconds +=1;//지금은 1초주기 1초 추가방식 
             //PROCESS NAME -> TIME + 1씩 추가? OR 시간 뺴기방식
        }
    }catch (Exception exception){
         exception.printStackTrace();
    }
         } ,1, 1, TimeUnit.SECONDS);//(코드 , 처음시작딜레이 , 주기 ,단위 second)
    }
}   

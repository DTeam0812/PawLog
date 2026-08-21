package com.pawlog.server.net;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 서버 전체에서 로그인된 유저의 활성 소켓 세션을 관리하는 싱글톤 레지스트리.
 *
 * 동일한 userId로 새 로그인이 들어오면 기존 세션에 SESSION_FORCE_LOGOUT 푸시를 전송하고
 * 새 세션으로 교체한다.
 */
public class SessionRegistry {
    private static final SessionRegistry INSTANCE = new SessionRegistry();

    // userId → 활성 ClientThread 맵 (스레드 안전)
    private final ConcurrentHashMap<Long, ClientThread> sessions = new ConcurrentHashMap<>();

    private SessionRegistry() {}
    public static SessionRegistry getInstance() {
        return INSTANCE;
    }
    /**
     * 신규 로그인 세션을 등록한다.
     * 동일 userId의 기존 세션이 존재하면 해당 세션에 강제 로그아웃 푸시를 보낸다.
     *
     * @param userId 로그인한 유저의 ID
     * @param thread 신규 로그인의 ClientThread
     */
    public void register(Long userId, ClientThread thread) {
        ClientThread old = sessions.put(userId, thread);

        if (old != null && old != thread) {
            System.out.printf("[SessionRegistry] userId=%d 중복 로그인 감지 → 기존 세션 강제 로그아웃%n", userId);
            old.forceLogout();
        }
    }

    /**
     * 세션을 제거한다.
     * thread가 현재 등록된 세션과 동일할 때만 제거한다.
     * (새 세션이 이미 등록된 상태에서 기존 세션 종료 시 새 세션이 지워지는 것을 방지)
     *
     * @param userId 제거할 유저 ID
     * @param thread 제거를 요청하는 ClientThread (본인 확인용)
     */
    public void remove(Long userId, ClientThread thread) {
        if (userId == null) return;
        sessions.remove(userId, thread); // value가 thread와 같을 때만 원자적으로 제거
    }

    /** 현재 등록된 활성 세션 수 (디버깅용) */
    public int size() {
        return sessions.size();
    }
}

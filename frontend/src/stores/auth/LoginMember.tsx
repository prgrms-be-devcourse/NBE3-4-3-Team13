"use client";

import { createContext, useState } from "react";

type Member = {
    id: number;
    username: string;
    nickname: string;
    password: string;
    createdAt: string;
    modifiedAt: string;
    provider: string;
    authorities: string[];
}

export const LoginMemberContext = createContext<{
    loginMember: Member;
    setLoginMember: (member: Member) => void;
    removeLoginMember: () => void;
    isLogin: boolean;
    isLoginMemberPending: boolean;
    isAdmin: boolean;
    setNoLoginMember: () => void;
}>({
    loginMember: createEmptyMember(),
    setLoginMember: () => {},
    removeLoginMember: () => {},
    isLogin: false,
    isLoginMemberPending: true,
    isAdmin: false,
    setNoLoginMember: () => {},
});

function createEmptyMember(): Member {
    return {
        id: 0,
        username: "",
        nickname: "",
        password: "",
        createdAt: "",
        modifiedAt: "",
        provider: "",
        authorities: [],
    };


}
export function useLoginMember() {
    const [isLoginMemberPending, setLoginMemberPending] = useState(true);
    const [loginMember, _setLoginMember] = useState<Member>(createEmptyMember());

    const removeLoginMember = () => {
        _setLoginMember(createEmptyMember());
        setLoginMemberPending(false);
    };

    const setLoginMember = (member: Member) => {
        _setLoginMember(member);
        setLoginMemberPending(false);
    };

    const setNoLoginMember = () => {
        setLoginMemberPending(false);
    };

    const isLogin = loginMember.id !== 0;
    // 권한 배열에 'ROLE_ADMIN'이 포함되어 있는지 확인
    const isAdmin = isLogin && loginMember.authorities?.includes('ROLE_ADMIN') || false;

    return {
        loginMember,
        removeLoginMember,
        isLogin,
        isLoginMemberPending,
        setLoginMember,
        isAdmin,
        setNoLoginMember,
    };
}
'use client';

import { useParams } from 'next/navigation';
import ClientPage from './ClientPage';

export default function Page() {
    const { groupId } = useParams<{ groupId: string }>();

    if (!groupId) {
        return <div>잘못된 접근입니다.</div>;
    }

    return <ClientPage groupId={groupId} />;
}

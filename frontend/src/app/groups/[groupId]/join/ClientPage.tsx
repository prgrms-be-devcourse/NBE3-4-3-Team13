'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';

interface Props {
    groupId: string;
}

export default function ClientPage({ groupId }: Props) {
    const router = useRouter();
    const [context, setContext] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsSubmitting(true);
        setError(null);

        try {
            const token = localStorage.getItem('accessToken');
            if (!token) {
                setError('로그인이 필요합니다.');
                setIsSubmitting(false);
                return;
            }

            const response = await fetch(`http://localhost:8080/api/v1/groups/${groupId}`, {
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                body: JSON.stringify({ context }),
            });

            const result = await response.json();

            if (!response.ok) {
                if (result.code === 'MA006') {
                    setError('이미 가입 신청된 회원입니다.');
                } else {
                    throw new Error(result.message || '모임 가입 신청에 실패했습니다.');
                }
                return;
            }

            alert('가입 신청이 완료되었습니다.');
            router.push(`/groups/${groupId}`);
        } catch (error) {
            setError(error instanceof Error ? error.message : '모임 가입 신청 중 오류가 발생했습니다.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className='max-w-xl mx-auto px-6 py-12'>
            <h1 className='text-2xl font-bold mb-6'>모임 가입 신청</h1>
            <form onSubmit={handleSubmit} className='space-y-6 bg-white dark:bg-gray-800 p-8 rounded-lg shadow-lg'>
                <label className='block'>
                    <span className='text-gray-700 dark:text-gray-200'>가입 신청 메시지</span>
                    <textarea
                        className='mt-2 w-full p-3 border rounded-md dark:bg-gray-700 dark:border-gray-600 dark:text-white'
                        rows={6}
                        value={context}
                        onChange={(e) => setContext(e.target.value)}
                        placeholder='간단한 자기소개나 가입 이유를 적어주세요.'
                        required
                    />
                </label>

                {error && <div className='text-red-600'>{error}</div>}

                <Button
                    type='submit'
                    disabled={isSubmitting}
                    className='text-sm px-5 py-2.5 bg-emerald-600 hover:bg-emerald-700 self-start'
                >
                    {isSubmitting ? '신청 중...' : '가입 신청'}
                </Button>
            </form>
        </div>
    );
}

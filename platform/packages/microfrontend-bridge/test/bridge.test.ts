import {describe, expect, it, vi} from 'vitest';
import {emitToHost, getHostContext, isDynamiaHost} from '../src/bridge.js';

describe('isDynamiaHost', () => {
    it('is true when dynamiaEmit is a function', () => {
        expect(isDynamiaHost({dynamiaEmit: () => undefined})).toBe(true);
    });

    it('is false when dynamiaEmit is missing or not a function', () => {
        expect(isDynamiaHost({})).toBe(false);
        expect(isDynamiaHost(null)).toBe(false);
        expect(isDynamiaHost(undefined)).toBe(false);
    });
});

describe('emitToHost', () => {
    it('calls dynamiaEmit with the given data', () => {
        const dynamiaEmit = vi.fn();
        emitToHost({dynamiaEmit}, {foo: 'bar'});
        expect(dynamiaEmit).toHaveBeenCalledWith({foo: 'bar'});
    });

    it('warns instead of throwing when dynamiaEmit is missing', () => {
        const warn = vi.spyOn(console, 'warn').mockImplementation(() => undefined);
        expect(() => emitToHost({}, {foo: 'bar'})).not.toThrow();
        expect(warn).toHaveBeenCalledOnce();
        warn.mockRestore();
    });
});

describe('getHostContext', () => {
    it('returns dynamiaHost when present', () => {
        expect(getHostContext({dynamiaHost: {tenantId: 't1'}})).toEqual({tenantId: 't1'});
    });

    it('returns {} when absent', () => {
        expect(getHostContext({})).toEqual({});
        expect(getHostContext(null)).toEqual({});
        expect(getHostContext(undefined)).toEqual({});
    });
});

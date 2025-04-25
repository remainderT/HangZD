local ip = KEYS[1]
local timeWindow = tonumber(ARGV[1]) -- 时间窗口，单位：秒

local accessKey = "hangzd:user:flow-risk-control:" .. ip

local currentAccessCount = redis.call("INCR", accessKey)

if currentAccessCount == 1 then
    redis.call("EXPIRE", accessKey, timeWindow)
end

return currentAccessCount

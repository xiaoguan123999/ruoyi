-- KEYS[1]: public stock hash
-- KEYS[2]: exclusive stock hash
-- ARGV[1]: target prize id
-- ARGV[2]: stock mode PUBLIC / EXCLUSIVE / AUTO
-- ARGV[3]: fallback prize id (optional)
-- return: E|prizeId / P|prizeId / F|prizeId / -2

local function try_deduct(stock_key, prize_id)
    if prize_id == nil or prize_id == false or prize_id == "" then
        return nil
    end
    if redis.call("hexists", stock_key, prize_id) == 0 then
        return nil
    end
    local current_stock = tonumber(redis.call("hget", stock_key, prize_id))
    if current_stock == nil then
        return nil
    end
    if current_stock == -1 then
        return prize_id
    end
    if current_stock > 0 then
        redis.call("hincrby", stock_key, prize_id, -1)
        return prize_id
    end
    return nil
end

local target_prize_id = ARGV[1]
local mode = ARGV[2]
local fallback_prize_id = ARGV[3]
if mode == nil or mode == "" then
    mode = "AUTO"
end

local won = nil
if mode == "PUBLIC" then
    won = try_deduct(KEYS[1], target_prize_id)
    if won then
        return "P|" .. won
    end
elseif mode == "EXCLUSIVE" then
    won = try_deduct(KEYS[2], target_prize_id)
    if won then
        return "E|" .. won
    end
else
    -- AUTO: exclusive first, then public same prize
    won = try_deduct(KEYS[2], target_prize_id)
    if won then
        return "E|" .. won
    end
    won = try_deduct(KEYS[1], target_prize_id)
    if won then
        return "P|" .. won
    end
end

won = try_deduct(KEYS[1], fallback_prize_id)
if won then
    return "F|" .. won
end

return "-2"

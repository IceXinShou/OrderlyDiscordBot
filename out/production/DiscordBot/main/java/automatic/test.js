// 定義基本參數
let earth = 0, fire = 0, water = 0, change = '', time = 0, count = 0, type = '', n = 0;
let types = ['earth', 'fire', 'water'];


// 生成預設 4 個搜集器
hero.spawnCollector();
hero.spawnCollector();
hero.spawnCollector();

// Main
while (true) {

    autoShield();

    // 搜集器小於 3 時自動產生
    if (hero.findMyCollectors().length < 3) {
        hero.spawnCollector();
    }

    // 時間超過 60 秒後開始主動攻擊
    if (hero.time > 60) {
        attack();
    }
}

/**
 * 平均蒐集物資
 */
hero.chooseItem = function (collector) {
    count++;
    return ['earth', 'fire', 'water'][count % 3];
};

/**
 * 主動攻擊
 */
function attack() {

    let em = [earth, fire, water];
    let min = Math.min(earth, fire, water);
    let type = (min < 3) ? types[em.findIndex(Math.min(earth, fire, water))] : '';

    if (type !== '') {
        for (let i; i === n; i++) {

            if (hero.canCast(type + '-arrow')) {
                n++;
                hero.cast(type + "-arrow");
            }

            if (i === 3) {
                type = '';
                break;
            }
        }

    }
    n = 0;
    type = '';
}


/**
 * 被動防禦
 */
function autoShield() {

    // 定義基本參數
    let enemy = hero.getEnemyHero();

    // 判斷攻擊屬性
    if (fire > enemy.fire) {
        change = 'fire';
    } else if (water > enemy.water) {
        change = 'water';
    } else if (earth > enemy.earth) {
        change = 'earth';
    }

    // 設定紀錄
    earth = enemy.earth;
    fire = enemy.fire;
    water = enemy.water;

    // 防禦
    if (change.length > 0) {
        hero.cast(change + '-shield');
        change = '';
    }
}

function print(text) {
    console.log('[console] ' + text);
}

console.clear();

class Die {
    constructor($container) {
        this._createElement($container);
        this._loadElements();
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.step = 0;
        this.results = { win: 0, lose: 0, tie: 0, total: 0 };
    }

    output() {
        this.index = Math.floor(Math.random() * 6);
        this.__rolling = false;
        this.$el.classList.remove('rolling');
        this._setFinalCoords();
    }

    complete(die) {
        this.results.total++;
        if (die.value > this.value)
            this.results.lose++;
        else if (die.value < this.value)
            this.results.win++;
        else
            this.results.tie++;
        this.$win.innerHTML = this.results.win;
        this.$lose.innerHTML = this.results.lose;
        this.$tie.innerHTML = this.results.tie;
        this.$wrate.innerHTML =
            Math.round(this.results.win / this.results.total * 100) + '%';
        this.$lrate.innerHTML =
            Math.round(this.results.lose / this.results.total * 100) + '%';
        this.$trate.innerHTML =
            Math.round(this.results.tie / this.results.total * 100) + '%';
    }

    rotate() {
        if (!this.__rolling) {
            this.__rolling = true;
            this.$el.classList.add('rolling');
        }
        this.step += 0.01;
        this.x += 5;
        this.y += 20;
        this.z += 5;
        this._setTransform();
    }

    get value() { return this.sides[this.index] }

    _createElement($container) {
        this.$el = document.createElement('div');
        this.$el.className = `die ${this.type}`;
        for (let i = 0; i < 6; i++) {
            let $side = document.createElement('div'),
                value = this.sides[i];
            $side.setAttribute('value', value);
            for (let j = 0; j < value; j++)
                $side.innerHTML += '<span></span>';
            this.$el.appendChild($side);
        }
        $container.appendChild(this.$el);
    }

    _loadElements() {
        let $results = document.querySelector(`.${this.type}-results`);
        this.$win   = $results.querySelector('p:last-child > span:nth-child(1) span:nth-child(2)');
        this.$wrate = $results.querySelector('p:last-child > span:nth-child(1) span:nth-child(3)');
        this.$lose  = $results.querySelector('p:last-child > span:nth-child(2) span:nth-child(2)');
        this.$lrate = $results.querySelector('p:last-child > span:nth-child(2) span:nth-child(3)');
        this.$tie   = $results.querySelector('p:last-child > span:nth-child(3) span:nth-child(2)');
        this.$trate = $results.querySelector('p:last-child > span:nth-child(3) span:nth-child(3)');
    }

    _setFinalCoords() {
        let coords = [
            [-95, 0, 5],
            [-5, 95, 0],
            [-5, 5, 0],
            [175, 85, 0],
            [85, 0, -5],
            [-5, 185, 0]
        ][this.index];

        this.x = coords[0];
        this.y = coords[1];
        this.z = coords[2];

        this._setTransform();
    }

    _setTransform() {
        let transform =
            `rotateX(${this.x}deg) rotateY(${this.y}deg) rotateZ(${this.z}deg)`;
        this.$el.style.webkitTransform = transform;
        this.$el.style.transform = transform;
    }
}

class DieA extends Die {
    get sides() { return [1, 2, 3, 4, 5, 6] }
    get type() { return 'dieA' }
}

class DieB extends Die {
    get sides() { return [1, 2, 3, 4, 5, 6] }
    get type() { return 'dieB' }
}

class DieC extends Die {
    get sides() { return [1, 2, 3, 4, 5, 6] }
    get type() { return 'dieC' }
}

class DieD extends Die {
    get sides() { return [1, 2, 3, 4, 5, 6] }
    get type() { return 'dieD' }
}

class DieE extends Die {
    get sides() { return [1, 2, 3, 4, 5, 6] }
    get type() { return 'dieE' }
}

let $container = document.querySelector('.dice'),
    dieA = new DieA($container),
    dieB = new DieB($container),
    dieC = new DieB($container),
    dieD = new DieB($container),
    dieE = new DieB($container),
    rolling = false,
    power = false;

document.getElementById('rollBtn').addEventListener('click', () => rollDice(0));
document.body.addEventListener('touchend', () => togglePower());

rollDice(0);

function togglePower() {
    power = !power;
    if (power) document.body.classList.add('power');
    else document.body.classList.remove('power');
}

function rollDice(count) {
    if (!power) rotate();
    if (count < 20 || (power && count < 1))
        window.requestAnimationFrame(() => rollDice(count + 1));
    else output();
}

function output() {
    dieA.output();
    dieB.output();
    dieC.output();
    dieD.output();
    dieE.output();

    let result = [dieA.value, dieB.value, dieC.value, dieD.value, dieE.value];
    //console.log(result)
    const scores = calculateYachtScore(result);
    //console.log(scores);
    // sendDiceResults('data', "채팅 메시지입니다.")
    // dieB.complete(dieC);
    // dieC.complete(dieA);
    // dieD.complete(dieA);
    // dieE.complete(dieA);
    // let ms = power ? 150 : 1000;
    // setTimeout(() => rollDice(0), ms);
}

function rotate() {
    dieA.rotate();
    dieB.rotate();
    dieC.rotate();
    dieD.rotate();
    dieE.rotate();
}

function calculateYachtScore(result) {
    const frequency = {};
    let score = 0;

    for (let i = 0; i < result.length; i++) {
        const dice = result[i];
        frequency[dice] = (frequency[dice] || 0) + 1;
        score += dice;
    }

    const scores = {
        'Number 1': 0,
        'Number 2': 0,
        'Number 3': 0,
        'Number 4': 0,
        'Number 5': 0,
        'Number 6': 0,
        'Bonus': 0,
        'Three of a Kind': 0,
        'Four of a Kind': 0,
        'Full House': 0,
        'Small Straight': 0,
        'Large Straight': 0,
        'Chance': 0,
        'Yacht': 0,
        'Yacht Bonus': 0
    };

    // Yacht (모든 주사위가 동일한 숫자인 경우)
    if (Object.keys(frequency).length === 1) {
        scores['Yacht'] = 50;
    }

    // Aces, Twos, Threes, Fours, Fives, Sixes
    let totalNumberScore = 0;

    for (let i = 1; i <= 6; i++) {
        const numberScore = frequency[i] ? i * frequency[i] : 0;
        scores[`Number ${i}`] = numberScore;
        totalNumberScore += numberScore;
    }

    // Bonus
    if (totalNumberScore >= 63) {
        scores['Bonus'] = 35;
    }

    // Four of a Kind
    for (let dice in frequency) {
        if (frequency[dice] >= 4) {
            scores['Four of a Kind'] = score;
            break;
        }
    }

    // Full House
    let hasThreeOfKind = false;
    let hasPair = false;

    for (let dice in frequency) {
        if (frequency[dice] === 3) {
            hasThreeOfKind = true;
        } else if (frequency[dice] === 2) {
            hasPair = true;
        }
    }

    if (hasThreeOfKind && hasPair) {
        scores['Full House'] = 25;
    }

    // Small Straight
    const sortedResult = [...new Set(result)].sort((a, b) => a - b);
    const smallStraight = [1, 2, 3, 4, 5];

    if (JSON.stringify(sortedResult) === JSON.stringify(smallStraight)) {
        scores['Small Straight'] = 30;
    }

    // Large Straight
    const largeStraight = [2, 3, 4, 5, 6];

    if (JSON.stringify(sortedResult) === JSON.stringify(largeStraight)) {
        scores['Large Straight'] = 40;
    }

    // Three of a Kind
    for (let dice in frequency) {
        if (frequency[dice] >= 3) {
            scores['Three of a Kind'] = score;
            break;
        }
    }

    // Chance
    scores['Chance'] = score;

    return scores;
}